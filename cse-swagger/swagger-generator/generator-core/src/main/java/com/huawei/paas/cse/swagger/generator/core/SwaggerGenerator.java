/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.cse.swagger.generator.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.springframework.util.StringUtils;

import io.swagger.models.Info;
import io.swagger.models.Swagger;

/**
 * 根据class提取swagger信息
 * 支持以下场景的使用：
 * 1.pojo + swagger annotation
 * 2.pojo + swagger annotation + jaxrs annotation
 * 3.pojo + swagger annotation + springmvc annotation
 * 场景1，使用默认规则处理rest入参，比如将所有参数包装为一个class，统一放在body中
 * jaxrs/spring mvc的场景，需要配合各自的插件jar包
 * 非场景1，如果标注不完整，仍然会尝试使用场景1的规则产生默认数据
 *
 * @author
 * @version  [版本号, 2017年3月1日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class SwaggerGenerator {
    protected SwaggerGeneratorContext context;

    // 需要生成class时，使用这个值
    protected String packageName;

    // 动态生成的class，加载在该classLoader中
    // 如果为null，表示加载在线程相关的classLoader中
    protected ClassLoader classLoader;

    protected Class<?> cls;

    protected Swagger swagger;

    private Map<String, OperationGenerator> operationGeneratorMap = new HashMap<>();

    private String httpMethod;

    public SwaggerGenerator(SwaggerGeneratorContext context, Class<?> cls) {
        this.swagger = new Swagger();
        this.context = context;
        this.cls = cls;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.packageName = "gen.swagger";
    }

    /**
     * 获取context的值
     * @return 返回 context
     */
    public SwaggerGeneratorContext getContext() {
        return context;
    }

    /**
     * 对packageName进行赋值
     * @param packageName packageName的新值
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String ensureGetPackageName() {
        // 不自动指定package，还是事先规划会比较合适
        if (packageName == null) {
            throw new Error("package name must not be null.");
        }

        return packageName;
    }

    /**
     * 获取classLoader的值
     * @return 返回 classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 对classLoader进行赋值
     * @param classLoader classLoader的新值
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 获取swagger的值
     * @return 返回 swagger
     */
    public Swagger getSwagger() {
        return swagger;
    }

    /**
     * 获取cls的值
     * @return 返回 cls
     */
    public Class<?> getCls() {
        return cls;
    }

    /**
     * 获取httpMethod的值
     * @return 返回 httpMethod
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * 对httpMethod进行赋值
     * @param httpMethod httpMethod的新值
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod.toLowerCase(Locale.US);
    }

    public Swagger generate() {
        for (Annotation annotation : cls.getAnnotations()) {
            ClassAnnotationProcessor processor = context.findClassAnnotationProcessor(annotation.annotationType());
            if (processor == null) {
                continue;
            }
            processor.process(annotation, this);
        }

        scanMethods();
        addOperationsToSwagger();

        correctSwagger();

        return swagger;
    }

    /**
     * 查找必填但是没值的字段，将之设置为默认值
     * 如果无法构造默认值，则抛出异常
     * @param cls
     * @param swagger
     */
    protected void correctSwagger() {
        if (StringUtils.isEmpty(swagger.getSwagger())) {
            swagger.setSwagger("2.0");
        }

        correctBasePath();
        correctInfo();
        correctProduces();
        correctConsumes();
    }

    private void correctProduces() {
        List<String> produces = swagger.getProduces();
        if (produces == null) {
            produces = Arrays.asList(MediaType.APPLICATION_JSON);
            swagger.setProduces(produces);
        }
    }

    private void correctConsumes() {
        List<String> consumes = swagger.getConsumes();
        if (consumes == null) {
            consumes = Arrays.asList(MediaType.APPLICATION_JSON);
            swagger.setConsumes(consumes);
        }
    }

    protected void correctBasePath() {
        String basePath = swagger.getBasePath();
        if (StringUtils.isEmpty(basePath)) {
            basePath = "/" + cls.getSimpleName();
        }
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        swagger.setBasePath(basePath);
    }

    private void correctInfo() {
        Info info = swagger.getInfo();
        if (info == null) {
            info = new Info();
            swagger.setInfo(info);
        }

        if (StringUtils.isEmpty(info.getTitle())) {
            info.setTitle("swagger definition for " + cls.getName());
        }
        if (StringUtils.isEmpty(info.getVersion())) {
            info.setVersion("1.0.0");
        }

        setJavaInterface(info, cls);
    }

    protected void setJavaInterface(Info info, Class<?> cls) {
        if (cls.isInterface()) {
            info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, cls.getName());
            return;
        }

        //        Class<?>[] interfaces = cls.getInterfaces();
        //        if (interfaces.length == 1) {
        //            info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, interfaces[0].getName());
        //            return;
        //        }

        String intfName = ensureGetPackageName() + "." + cls.getSimpleName() + "Intf";
        info.setVendorExtension(SwaggerConst.EXT_JAVA_INTF, intfName);
    }

    protected boolean isSkipMethod(Method method) {
        // jaxrs实现中跳过了volatile的方法，按同样的逻辑处理
        if (Modifier.isVolatile(method.getModifiers())) {
            return true;
        }

        return method.getDeclaringClass().getName().equals(Object.class.getName());
    }

    protected void scanMethods() {
        for (Method method : cls.getMethods()) {
            if (isSkipMethod(method)) {
                continue;
            }

            OperationGenerator operationGenerator = new OperationGenerator(this, method);
            operationGenerator.generate();

            String operationId = operationGenerator.getOperation().getOperationId();
            if (operationGeneratorMap.containsKey(operationId)) {
                throw new Error(String.format("OperationId must be unique. %s:%s", cls.getName(), method.getName()));
            }
            operationGeneratorMap.put(operationId, operationGenerator);
        }
    }

    protected void addOperationsToSwagger() {
        for (OperationGenerator operationGenerator : operationGeneratorMap.values()) {
            operationGenerator.addOperationToSwagger();
        }
    }

    public void setBasePath(String basePath) {
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        swagger.setBasePath(basePath);
    }

    /**
     * 获取operationGeneratorMap的值
     * @return 返回 operationGeneratorMap
     */
    public Map<String, OperationGenerator> getOperationGeneratorMap() {
        return operationGeneratorMap;
    }
}
