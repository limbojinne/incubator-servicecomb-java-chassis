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

package com.huawei.paas.cse.demo.jaxrs.server;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.huawei.paas.cse.common.rest.codec.RestObjectMapper;
import com.huawei.paas.cse.core.Response;
import com.huawei.paas.cse.core.context.ContextUtils;
import com.huawei.paas.cse.demo.compute.Person;
import com.huawei.paas.cse.demo.server.User;
import com.huawei.paas.cse.provider.rest.common.RestSchema;
import com.huawei.paas.cse.swagger.extend.annotations.ResponseHeaders;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

@RestSchema(schemaId = "codeFirst")
@Path("/codeFirstJaxrs")
@Produces(MediaType.APPLICATION_JSON)
public class CodeFirstJaxrs {
    //    public Response getUserResponse() {
    //
    //    }
    @ApiResponse(code = 200, response = User.class, message = "")
    @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class),
            @ResponseHeader(name = "h2", response = String.class)})
    @Path("/cseResponse")
    @GET
    public Response cseResponse() {
        Response response = Response.createSuccess(Status.ACCEPTED, new User());
        response.getHeaders().addHeader("h1", "h1v").addHeader("h2", "h2v");
        return response;
    }

    @Path("/bytes")
    @POST
    public byte[] bytes(byte[] input) {
        input[0] = (byte) (input[0] + 1);
        return input;
    }

    @Path("/addDate")
    @POST
    public Date addDate(@FormParam("date") Date date, @QueryParam("seconds") long seconds) {
        return new Date(date.getTime() + seconds * 1000);
    }

    @GET
    public int defaultPath() {
        return 100;
    }

    @Path("/add")
    @POST
    public int add(@FormParam("a") int a, @FormParam("b") int b) {
        return a + b;
    }

    @Path("/reduce")
    @GET
    @ApiImplicitParams({@ApiImplicitParam(name = "a", dataType = "integer", format = "int32", paramType = "query")})
    public int reduce(HttpServletRequest request, @CookieParam("b") int b) {
        int a = Integer.parseInt(request.getParameter("a"));
        return a - b;
    }

    @Path("/sayhello")
    @POST
    public Person sayHello(Person user) {
        user.setName("hello " + user.getName());
        return user;
    }

    @SuppressWarnings("unchecked")
    @Path("/testrawjson")
    @POST
    public String testRawJsonString(String jsonInput) {
        Map<String, String> person;
        try {
            person = RestObjectMapper.INSTANCE.readValue(jsonInput.getBytes(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return "hello " + person.get("name");
    }

    @Path("/saysomething")
    @POST
    public String saySomething(@HeaderParam("prefix") String prefix, Person user) {
        return prefix + " " + user.getName();
    }

    @Path("/sayhi/{name}")
    @PUT
    public String sayHi(@PathParam("name") String name) {
        ContextUtils.getInvocationContext().setStatus(202);
        return name + " sayhi";
    }

    @Path("/sayhi/{name}/v2")
    @PUT
    public String sayHi2(@PathParam("name") String name) {
        return name + " sayhi 2";
    }

    @Path("/istrue")
    @GET
    public boolean isTrue() {
        return true;
    }

    @Path("/addstring")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String addString(@QueryParam("s") List<String> s) {
        String result = "";
        for (String x : s) {
            result += x;
        }
        return result;
    }

}
