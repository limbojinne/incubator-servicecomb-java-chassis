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

package com.huawei.paas.foundation.vertx;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.huawei.paas.foundation.common.net.URIEndpointObject;
import com.huawei.paas.foundation.vertx.client.tcp.TcpClient;
import com.huawei.paas.foundation.vertx.client.tcp.TcpClientConfig;
import com.huawei.paas.foundation.vertx.client.tcp.TcpClientPool;
import com.huawei.paas.foundation.vertx.client.tcp.TcpClientVerticle;
import com.huawei.paas.foundation.vertx.client.tcp.TcpData;
import com.huawei.paas.foundation.vertx.client.tcp.TcpRequest;
import com.huawei.paas.foundation.vertx.client.tcp.TcpResonseCallback;
import com.huawei.paas.foundation.vertx.server.TcpParser;
import com.huawei.paas.foundation.vertx.server.TcpServer;
import com.huawei.paas.foundation.vertx.tcp.TcpOutputStream;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.SocketAddressImpl;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class TestTcp {

    enum ParseStatus {
        MSG_ID_AND_LEN, // len: total len/header len
        HEADER,
        BODY
    }

    /**
     * Test TcpClient
     * @throws Exception 
     */
    @Test
    public void testTcpClient() throws Exception {
        NetClient oNetClient = new NetClient() {

            @Override
            public boolean isMetricsEnabled() {
                // TODO Auto-generated method stub
                return true;
            }

            @Override
            public NetClient connect(int port, String host, Handler<AsyncResult<NetSocket>> connectHandler) {
                // TODO Auto-generated method stub

                return Mockito.mock(NetClient.class);
            }

            @Override
            public void close() {
                // TODO Auto-generated method stub

            }
        };
        TcpClient oTcpClient =
            new TcpClient(Mockito.mock(Context.class), oNetClient, "highway://127.2.0.1:8080", new TcpClientConfig());
        oTcpClient.checkTimeout();
        oTcpClient.send(new TcpOutputStream(), 123, Mockito.mock(TcpResonseCallback.class));
        oTcpClient.send(new TcpOutputStream(), 123, Mockito.mock(TcpResonseCallback.class));
        Assert.assertNotEquals(null, oTcpClient.getContext());

        new MockUp<TcpClientPool>() {
            @Mock
            protected void startCheckTimeout(TcpClientConfig clientConfig, Context context) {
            }
        };
        TcpClientConfig config = new TcpClientConfig();
        TcpClientPool oClientPool =
            new TcpClientPool(config, Vertx.vertx().getOrCreateContext(), oNetClient);
        oClientPool.send("highway://152.2.2.3:8080", new TcpOutputStream(), Mockito.mock(TcpResonseCallback.class));
        oClientPool.send("highway://152.2.2.3:8080", new TcpOutputStream(), Mockito.mock(TcpResonseCallback.class));
        Assert.assertNotNull(oClientPool);

        TcpRequest oTcpRequest = new TcpRequest(1234, Mockito.mock(TcpResonseCallback.class));
        oTcpRequest.isTimeout();
        oTcpRequest.onReply(Buffer.buffer(), Buffer.buffer(("test").getBytes()));
        oTcpRequest.onSendError(new Throwable("test Errorsss"));
        Assert.assertNotNull(oTcpRequest);

        TcpClientVerticle oTcpClientVerticle = new TcpClientVerticle();
        oTcpClientVerticle.init(Vertx.vertx(), Vertx.vertx().getOrCreateContext());
        oTcpClientVerticle.createClientPool();
        oTcpClientVerticle.createClientPool();
        Assert.assertNotNull(oTcpClientVerticle.getVertx());

        NetSocket socket = Mockito.mock(NetSocket.class);
        Throwable e = Mockito.mock(Throwable.class);
        Buffer hBuffer = Mockito.mock(Buffer.class);
        Buffer bBuffer = Mockito.mock(Buffer.class);

        Deencapsulation.invoke(oTcpClient, "connect");
        Deencapsulation.invoke(oTcpClient, "onConnectSuccess", socket);
        Mockito.when(socket.localAddress()).thenReturn(new SocketAddressImpl(0, "127.0.0.1"));
        Deencapsulation.setField(oTcpClient, "netSocket", socket);
        Deencapsulation.invoke(oTcpClient, "onDisconnected", e);
        Deencapsulation.invoke(oTcpClient, "tryLogin");
        Deencapsulation.invoke(oTcpClient, "onLoginSuccess");
        Deencapsulation.invoke(oTcpClient, "onConnectFailed", e);
        long l = 10;
        Deencapsulation.invoke(oTcpClient, "onReply", l, hBuffer, bBuffer);
        oTcpClient.checkTimeout();
        Assert.assertNotNull(oTcpClient);
    }

    /**
     * test TcpOutputStream
     */
    @Test
    public void testTcpOutputStream() {
        TcpOutputStream oStream = new TcpOutputStream();
        oStream.close();
        Buffer buffer = oStream.getBuffer();
        Assert.assertArrayEquals(TcpParser.TCP_MAGIC, buffer.getBytes(0, TcpParser.TCP_MAGIC.length));
        Assert.assertEquals(oStream.getMsgId(), buffer.getLong(TcpParser.TCP_MAGIC.length));
    }

    /**
     * test TcpServerStarter
     */
    @Test
    public void testTcpServerStarter() {
        URIEndpointObject endpiont = new URIEndpointObject("highway://127.0.0.1:9900");
        TcpServer oStarter = new TcpServer(endpiont);
        oStarter.init(Vertx.vertx(), "", null);
        Assert.assertNotNull(oStarter);
        //TODO Need to find a way to Assert TcpServerStarter as this obbject does not return any values.
    }

    @Test
    public void testTcpClientConfig() {
        TcpClientConfig tcpClientConfig = new TcpClientConfig();
        tcpClientConfig.getRequestTimeoutMillis();
        tcpClientConfig.setRequestTimeoutMillis(1);
        Assert.assertNotNull(tcpClientConfig);
    }

    @Test
    public void testTcpData() {
        Buffer hBuffer = Mockito.mock(Buffer.class);
        Buffer bBuffer = Mockito.mock(Buffer.class);
        TcpData tcpData = new TcpData(hBuffer, bBuffer);
        tcpData.getBodyBuffer();
        tcpData.setBodyBuffer(bBuffer);
        tcpData.getHeaderBuffer();
        tcpData.setBodyBuffer(hBuffer);
        Assert.assertNotNull(tcpData.getBodyBuffer());
        Assert.assertNotNull(tcpData.getHeaderBuffer());
    }
}
