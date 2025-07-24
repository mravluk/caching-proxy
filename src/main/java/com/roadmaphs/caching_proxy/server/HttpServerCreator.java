package com.roadmaphs.caching_proxy.server;

import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;

@Component
public class HttpServerCreator {

    private final static Logger logger = LoggerFactory.getLogger(HttpServerCreator.class);

    HttpServer httpServer = null;

    public HttpServer startProxyServer(Integer port, String url) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(url, port), 0);
            createContextForAllRequests(httpServer);
            httpServer.start();
            logger.info("Successfully started http server at {}:{}", url, port);
        }
        catch (IOException e) {
            logger.error("Can't create a server at given url and port!");
        }

        return httpServer;
    }

    private void createContextForAllRequests(HttpServer httpServer) {
        httpServer.createContext("/", exchange -> {
            
        });
    }

    @PreDestroy
    public void stopServer() {
        if (httpServer != null) {
            httpServer.stop(0); // Stop immediately
            httpServer = null;
            logger.info("Caching proxy server stopped.");
        }
    }
}
