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

package com.huawei.paas.cse.swagger.generator.core.processor.parameter;

import com.huawei.paas.cse.swagger.generator.core.OperationGenerator;
import com.huawei.paas.cse.swagger.generator.core.ParameterAnnotationProcessor;
import com.huawei.paas.cse.swagger.generator.core.utils.ParamUtils;

import io.swagger.models.parameters.AbstractSerializableParameter;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author
 * @version  [版本号, 2017年3月27日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class AbstractParameterProcessor<T extends AbstractSerializableParameter<?>>
        implements ParameterAnnotationProcessor {
    @Override
    public void process(Object annotation, OperationGenerator operationGenerator, int paramIdx) {
        T parameter = createParameter();

        fillParameter(annotation, operationGenerator, paramIdx, parameter);

        operationGenerator.addProviderParameter(parameter);
    }

    protected void fillParameter(Object annotation, OperationGenerator operationGenerator, int paramIdx,
            T parameter) {
        setParameterName(annotation, operationGenerator, paramIdx, parameter);
        setParameterType(operationGenerator, paramIdx, parameter);
    }

    protected void setParameterType(OperationGenerator operationGenerator, int paramIdx,
            T parameter) {
        ParamUtils.setParameterType(operationGenerator.getSwagger(),
                operationGenerator.getProviderMethod(),
                paramIdx,
                parameter);
    }

    protected void setParameterName(Object annotation, OperationGenerator operationGenerator, int paramIdx,
            T parameter) {
        String paramName = getAnnotationParameterName(annotation);
        paramName = ParamUtils.getParameterName(paramName, operationGenerator.getProviderMethod(), paramIdx);
        parameter.setName(paramName);
    }

    protected abstract T createParameter();

    protected abstract String getAnnotationParameterName(Object annotation);
}
