package org.smartboot.jade;

import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.jade.conf.BackendProxy;
import org.smartboot.jade.conf.Config;
import org.smartboot.jade.proxy.ProxyServerHandler;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.HttpRouteHandler;
import org.smartboot.http.server.handler.HttpStaticResourceHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * mvn -Pnative -Dagent=true -DskipTests clean package
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {
        //解析 -conf 参数
        String conf = "/Users/zhengjw22mac123/IdeaProjects/jade/src/main/resources/st.yaml";
        for (int i = 0; i < args.length; i++) {
            if (Constant.ARG_CONF.equals(args[i])) {
                conf = args[++i];
            }
        }
        if (StringUtils.isBlank(conf)) {
            System.err.println(Constant.ARG_CONF + " 参数未指定!");
            return;
        }

        Yaml yaml = new Yaml();
        Config config = yaml.loadAs(new FileInputStream(conf), Config.class);

        Map<String, BackendProxy> proxyServices = new HashMap<>();
        config.getServers().forEach(backendProxy -> proxyServices.put(backendProxy.getName(), backendProxy));

        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().serverName("st");


        HttpRouteHandler routeHandler = new HttpRouteHandler();

        config.getRoutes().forEach(route -> {
            if (StringUtils.isNotBlank(route.getBaseDir())) {
                routeHandler.route(route.getPath(), new HttpStaticResourceHandler(route.getBaseDir()));
            } else if (StringUtils.isNotBlank(route.getTarget())) {
                routeHandler.route(route.getPath()+"**", new ProxyServerHandler(route.getPath(),proxyServices.get(route.getTarget())));
            }
        });

        bootstrap.httpHandler(routeHandler);
        bootstrap.configuration().debug(true);
        bootstrap.setPort(config.getPort()).start();
    }
}