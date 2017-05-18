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

package com.huawei.paas.cse.swagger.generator.springmvc.processor.annotation;

import org.springframework.web.bind.annotation.RequestParam;

import com.huawei.paas.cse.swagger.generator.core.processor.parameter.AbstractParameterProcessor;

import io.swagger.models.parameters.QueryParameter;

public class RequestParamAnnotationProcessor extends AbstractParameterProcessor<QueryParameter> {
    /**
     * {@inheritDoc}
     */
    @Override
    protected QueryParameter createParameter() {
        return new QueryParameter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getAnnotationParameterName(Object annotation) {
        return ((RequestParam) annotation).name();
    }
}
