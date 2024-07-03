package org.smartboot.jade.proxy;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.client.Body;
import org.smartboot.http.client.Header;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.client.HttpRest;
import org.smartboot.http.client.ResponseHandler;
import org.smartboot.http.common.enums.BodyStreamStatus;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.impl.Request;
import org.smartboot.jade.conf.BackendProxy;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.factory.ClientSSLContextFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//反向代理服务
public class ProxyServerHandler extends HttpServerHandler {
    private BackendProxy backendProxy;
    private Map<Request, ProxyUnit> proxies = new ConcurrentHashMap<>();
    private String prefix;

    public ProxyServerHandler(String path, BackendProxy backendProxy) {
        this.backendProxy = backendProxy;
        this.prefix = path;
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        super.onHeaderComplete(request);
        HttpClient httpClient = new HttpClient(backendProxy.getHost(), backendProxy.getPort());
        try {
            httpClient.configuration().addPlugin(new SslPlugin<>(new ClientSSLContextFactory()));
            httpClient.configuration().debug(true).setWriteBufferSize(1024);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HttpRest rest = httpClient.rest(request.getRequestURI().substring(prefix.length() - 1)).setMethod(request.getMethod());
        proxies.put(request, new ProxyUnit(httpClient, rest, request.getRequestType()));


        Header header = rest.header();
        request.getHeaderNames().stream().filter(name -> !name.equals(HeaderNameEnum.HOST.getName())).forEach(name -> header.add(name, request.getHeader(name)));
        var clientSession = request.getAioSession();
        rest.onResponse(new ResponseHandler() {

            @Override
            public void onHeaderComplete(AbstractResponse abstractResponse) throws IOException {
                if (request.getRequestType() == HttpTypeEnum.HTTP) {
                    //将后端的响应反馈给前端
                    HttpResponse response = request.newHttpRequest().getResponse();
                    response.setHttpStatus(abstractResponse.getStatus(), abstractResponse.getReasonPhrase());
                    response.getOutputStream().disableChunked();
                    abstractResponse.getHeaderNames().forEach(name -> response.addHeader(name, abstractResponse.getHeader(name)));
                    response.getOutputStream().flush();
                }
            }

            @Override
            public BodyStreamStatus onBodyStream(ByteBuffer buffer, AbstractResponse request) {
                //将后端的响应反馈给前端
                var backendSession = request.getSession();
                backendSession.awaitRead();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
//                System.out.println(new String(bytes));
                if (bytes.length == 0) {
                    System.out.println("aaa");
                }
                try {
                    clientSession.writeBuffer().write(bytes, 0, bytes.length, writeBuffer -> {
                        backendSession.signalRead();
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return BodyStreamStatus.OnAsync;
            }
        });

        rest.body().flush();
    }

    @Override
    public BodyStreamStatus onBodyStream(ByteBuffer buffer, Request request) {
        //将前端的数据包投递给后台
        var proxy = proxies.get(request);
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        request.getAioSession().awaitRead();
        Body body = proxy.rest.body();
        body.write(bytes, 0, bytes.length, (Consumer<Body<? extends HttpRest>>) _ -> request.getAioSession().signalRead());
        return BodyStreamStatus.OnAsync;
    }

    @Override
    public void onClose(Request request) {
        var client = proxies.remove(request);
        if (client != null) {
            client.httpClient.close();
        }
    }

    class ProxyUnit {
        HttpClient httpClient;
        HttpRest rest;
        HttpTypeEnum httpType;

        public ProxyUnit(HttpClient httpClient, HttpRest rest, HttpTypeEnum httpType) {
            this.httpClient = httpClient;
            this.rest = rest;
            this.httpType = httpType;
        }
    }
}

