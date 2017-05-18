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

package com.huawei.paas.cse.core.definition.schema;

import java.net.URL;

import javax.inject.Inject;

import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.SchemaMeta;
import com.huawei.paas.cse.core.definition.SchemaUtils;
import com.huawei.paas.cse.core.definition.loader.SchemaLoader;
import com.huawei.paas.cse.serviceregistry.api.Const;
import com.huawei.paas.cse.swagger.generator.core.CompositeSwaggerGeneratorContext;
import com.huawei.paas.cse.swagger.generator.core.SwaggerGenerator;
import com.huawei.paas.cse.swagger.generator.core.SwaggerGeneratorContext;
import com.huawei.paas.cse.swagger.generator.core.utils.ClassUtils;

import io.swagger.models.Swagger;

/**
 * 由consumer或producer发起的契约注册
 * 在consumer场景中，如果本地没有契约，需要从服务中心下载契约
 * 在producer场景中，如果本地没有契约，需要根据实现类动态生成契约
 *
 * @author   
 * @version  [版本号, 2017年4月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractSchemaFactory<CONTEXT extends SchemaContext> {
    @Inject
    protected MicroserviceMetaManager microserviceMetaManager;

    @Inject
    protected SchemaLoader schemaLoader;

    @Inject
    protected CompositeSwaggerGeneratorContext compositeSwaggerGeneratorContext;

    // 因为aop的存在，schemaInstance的class不一定等于schemaClass
    protected SchemaMeta getOrCreateSchema(CONTEXT context) {
        MicroserviceMeta microserviceMeta = context.getMicroserviceMeta();
        SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(context.getSchemaId());
        if (schemaMeta == null) {
            schemaMeta = createSchema(context);
        }
        context.setSchemaMeta(schemaMeta);

        connectToProvider(context);

        return schemaMeta;
    }

    protected abstract void connectToProvider(CONTEXT context);

    protected abstract SchemaMeta createSchema(CONTEXT context);

    protected Swagger loadSwagger(CONTEXT context) {
        String path = generateSchemaPath(context.getMicroserviceName(), context.getSchemaId());
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            return null;
        }

        return SchemaUtils.parseSwagger(url);
    }

    protected String generateSchemaPath(String microserviceName, String schemaId) {
        int idxAt = microserviceName.indexOf(Const.APP_SERVICE_SEPARATOR);
        if (idxAt < 0) {
            return String.format("microservices/%s/%s.yaml", microserviceName, schemaId);
        }

        String appId = microserviceName.substring(0, idxAt);
        String realMicroserviceName = microserviceName.substring(idxAt + 1);
        return String.format("applications/%s/%s/%s.yaml",
                appId,
                realMicroserviceName,
                schemaId);
    }

    protected SwaggerGenerator generateSwagger(CONTEXT context) {
        SwaggerGeneratorContext generatorContext =
            compositeSwaggerGeneratorContext.selectContext(context.getProviderClass());
        SwaggerGenerator generator = new SwaggerGenerator(generatorContext, context.getProviderClass());
        generator.setPackageName(
                SchemaUtils.generatePackageName(context.getMicroserviceMeta(), context.getSchemaId()));
        generator.generate();

        // 确保接口是存在的
        ClassUtils.getOrCreateInterface(generator);
        return generator;
    }
}
