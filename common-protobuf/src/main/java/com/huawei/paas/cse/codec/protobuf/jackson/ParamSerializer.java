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

package com.huawei.paas.cse.codec.protobuf.jackson;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufGenerator;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;

/**
 * 将idl msg中的field拆出来，每个field是一个参数
 * @author   
 * @version  [版本号, 2016年12月6日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ParamSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen,
            SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();

        ProtobufGenerator protobufGenerator = (ProtobufGenerator) gen;
        Iterator<ProtobufField> iter = protobufGenerator.getSchema().getRootType().fields().iterator();
        Object[] values = (Object[]) value;
        for (int idx = 0; idx < values.length; idx++) {
            gen.writeObjectField(iter.next().name, values[idx]);
        }

        gen.writeEndObject();
    }

}
