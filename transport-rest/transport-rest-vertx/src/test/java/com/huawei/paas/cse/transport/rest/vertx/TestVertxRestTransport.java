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

package com.huawei.paas.cse.transport.rest.vertx;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.cse.core.AsyncResponse;
import com.huawei.paas.cse.core.Endpoint;
import com.huawei.paas.cse.core.Invocation;
import com.huawei.paas.foundation.common.net.URIEndpointObject;
import com.huawei.paas.foundation.vertx.VertxUtils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import mockit.Mock;
import mockit.MockUp;

public class TestVertxRestTransport {

    private VertxRestTransport instance = new VertxRestTransport();

    @Test
    public void testGetInstance() {
        Assert.assertNotNull(instance);
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("rest", instance.getName());

    }

    @Test
    public void testInit() {
        boolean status = false;
        try {
            new MockUp<VertxUtils>() {
                @Mock
                public Vertx init(VertxOptions vertxOptions) {
                    return null;

                }

                @Mock
                public <VERTICLE extends AbstractVerticle> boolean blockDeploy(Vertx vertx, Class<VERTICLE> cls,
                        DeploymentOptions options) throws InterruptedException {
                    return true;
                }
            };
            instance.init();
        } catch (Exception e) {
            status = true;
        }
        Assert.assertFalse(status);
    }

    @Test
    public void testSendException() {
        boolean validAssert;
        Invocation invocation = Mockito.mock(Invocation.class);
        AsyncResponse asyncResp = Mockito.mock(AsyncResponse.class);
        URIEndpointObject endpoint = Mockito.mock(URIEndpointObject.class);
        Endpoint end = Mockito.mock(Endpoint.class);
        Mockito.when(invocation.getEndpoint()).thenReturn(end);
        Mockito.when(invocation.getEndpoint().getAddress()).thenReturn(endpoint);
        try {
            validAssert = true;
            instance.send(invocation, asyncResp);
        } catch (Exception e) {

            validAssert = false;
        }
        Assert.assertFalse(validAssert);
    }

}
