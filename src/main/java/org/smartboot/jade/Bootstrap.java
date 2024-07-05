package org.smartboot.jade;

import org.smartboot.http.common.utils.CollectionUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.handler.HttpRouteHandler;
import org.smartboot.http.server.handler.HttpStaticResourceHandler;
import org.smartboot.jade.conf.BackendProxy;
import org.smartboot.jade.conf.Config;
import org.smartboot.jade.proxy.ProxyServerHandler;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * mvn -Pnative -Dagent=true -DskipTests clean package
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException, URISyntaxException {
        //解析 -conf 参数
//        String conf = "/Users/zhengjw22mac123/IdeaProjects/jade/src/main/resources/st.yaml";
        String conf = "";
        for (int i = 0; i < args.length; i++) {
            if (Constant.ARG_CONF.equals(args[i])) {
                conf = args[++i];
            }
        }
        Config config;
        boolean noneConf = true;
        if (StringUtils.isBlank(conf)) {
            config = new Config();
        } else {
            File file = new File(conf);
            if (!file.isFile()) {
                System.err.println("conf 配置文件不存在");
                System.exit(-1);
                return;
            }
            Yaml yaml = new Yaml();
            config = yaml.loadAs(new FileInputStream(file), Config.class);
            noneConf = false;
        }

        Map<String, BackendProxy> proxyServices = new HashMap<>();
        if (CollectionUtils.isNotEmpty(config.getServers())) {
            config.getServers().forEach(backendProxy -> proxyServices.put(backendProxy.getName(), backendProxy));
        }


        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().serverName("jade").readBufferSize(8 * 1024).writeBufferSize(8 * 1024);

        HttpRouteHandler routeHandler = new HttpRouteHandler();
        if (noneConf) {
            routeHandler.route("/**", new HttpStaticResourceHandler(Thread.currentThread().getContextClassLoader().getResource("static").toURI()));
        } else {
            config.getRoutes().forEach(route -> {
                if (StringUtils.isNotBlank(route.getBaseDir())) {
                    routeHandler.route(route.getPath(), new HttpStaticResourceHandler(route.getBaseDir()));
                } else if (StringUtils.isNotBlank(route.getTarget())) {
                    routeHandler.route(route.getPath() + "**", new ProxyServerHandler(route.getPath(), proxyServices.get(route.getTarget())));
                }
            });
        }

        bootstrap.httpHandler(routeHandler);


        bootstrap.configuration().debug(false);
        bootstrap.setPort(config.getPort()).start();
    }
}