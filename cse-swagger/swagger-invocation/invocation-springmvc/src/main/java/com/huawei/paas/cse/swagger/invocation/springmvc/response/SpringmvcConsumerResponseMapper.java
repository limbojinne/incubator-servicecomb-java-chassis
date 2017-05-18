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
package com.huawei.paas.cse.swagger.invocation.springmvc.response;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.swagger.invocation.response.consumer.ConsumerResponseMapper;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author
 * @version  [版本号, 2017年4月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class SpringmvcConsumerResponseMapper implements ConsumerResponseMapper {
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getResponseClass() {
        return ResponseEntity.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object mapResponse(Response response) {
        HttpStatus status = HttpStatus.valueOf(response.getStatusCode());

        HttpHeaders httpHeaders = null;
        Map<String, List<Object>> headers = response.getHeaders().getHeaderMap();
        if (headers != null) {
            httpHeaders = new HttpHeaders();
            for (Entry<String, List<Object>> entry : headers.entrySet()) {
                for (Object value : entry.getValue()) {
                    httpHeaders.add(entry.getKey(), String.valueOf(value));
                }
            }
        }

        return new ResponseEntity<Object>(response.getResult(), httpHeaders, status);
    }
}
