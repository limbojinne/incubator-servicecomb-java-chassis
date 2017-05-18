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

package com.huawei.paas.cse.common.rest.codec.produce;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JavaType;

/**
 * 用于处理text/plain类型的produce的processor
 * @author   
 * @version  [版本号, 2017年1月2日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ProduceTextPlainProcessor extends AbstractProduceProcessor {
    @Override
    public String getName() {
        return MediaType.TEXT_PLAIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void encodeResponse(OutputStream output, Object result) throws Exception {
        output.write(String.valueOf(result).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object decodeResponse(InputStream input, JavaType type) throws Exception {
        // plainText类型，肯定是返回string的，想不出有其他类型的场景
        return IOUtils.toString(input, StandardCharsets.UTF_8);
        // TODO: 该方法尚需进一步修改
        //        Class<?> returnCls = type.getRawClass();
        //        if (returnCls.isPrimitive()) {
        //            // 处理char类型
        //            if (returnCls == char.class) {
        //                return ((String)result).charAt(0);
        //            }
        //            // 处理其他如int, long, boolean等类型
        //            return RestObjectMapper.INSTANCE.readValue((String)result, type);
        //        }
        //        else {
        //            // 处理如String等其他复杂类型
        //            // 对于更多处理不了的复杂类型建议使用"application/json"
        //            return returnCls.getConstructor(new Class<?>[] {String.class})
        //                    .newInstance((String)result);
        //        }
    }

}
