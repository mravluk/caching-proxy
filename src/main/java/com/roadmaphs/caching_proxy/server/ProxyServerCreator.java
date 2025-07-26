package com.roadmaphs.caching_proxy.server;

import com.roadmaphs.caching_proxy.dto.CachedResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProxyServerCreator {

    private final static Logger logger = LoggerFactory.getLogger(ProxyServerCreator.class);
    private Map<String, CachedResponse> cache = new ConcurrentHashMap<>();
    HttpServer proxyServer = null;

    public HttpServer startProxyServer(Integer port, String origin) {
        try {
            proxyServer = HttpServer.create(new InetSocketAddress(port), 0);
            createContextForAllRequests(proxyServer, origin);
            proxyServer.start();
            logger.info("Successfully started HTTP proxy server at {}:{}", origin, port);
        }
        catch (IOException e) {
            logger.error("Can't create a server at given origin and port: " + e.getMessage());
        }
        return proxyServer;
    }

    private void createContextForAllRequests(HttpServer httpServer, String origin) {
        httpServer.createContext("/", exchange -> {
            try {
                String requestUri = exchange.getRequestURI().getPath();
                CachedResponse cachedResponse = cache.get(requestUri);

                //Cached response
                if (cachedResponse != null) {
                    sendResponse(exchange, cachedResponse, true);
                    return;
                }

                //Not cached response - need to forward the request to the origin, then if everything is okay save it..
                HttpURLConnection connection = createHttpURLConnection(origin, exchange, requestUri);
                CachedResponse responseToClient = createCachedResponse(connection);
                cache.put(requestUri, responseToClient);
                sendResponse(exchange, responseToClient, false);

                connection.disconnect();
                logger.info("Request successfully handled!");
            } catch (IOException e) {
                sendError(exchange, "Error forwarding request: " + e.getMessage());
            }
        });
    }

    private static HttpURLConnection createHttpURLConnection(String origin, HttpExchange exchange, String requestUri) throws IOException {
        URL originUrl = URI.create(origin + requestUri).toURL();
        HttpURLConnection connection = (HttpURLConnection) originUrl.openConnection();

        connection.setRequestMethod(exchange.getRequestMethod());
        exchange.getRequestHeaders().forEach(
                (key, values) -> {
                    if (!key.equalsIgnoreCase("Host")){
                        values.forEach(value -> connection.addRequestProperty(key, value));
                    }
                }
        );
        return connection;
    }

    private static CachedResponse createCachedResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        Map<String, String> headers = new HashMap<>();

        connection.getHeaderFields().forEach(
                (key, values) -> {
                        if (key != null)
                            headers.put(key, String.join(",", values));
                }
        );

        InputStream inputStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
        byte[] requestBody = inputStream != null ? inputStream.readAllBytes() : new byte[0];

        if (inputStream != null)
            inputStream.close();
        //cache the response then send response
        return new CachedResponse(requestBody, responseCode, headers);
    }


    private void sendResponse(HttpExchange exchange, CachedResponse response, boolean isFromCache) {
        response.getHeaders().forEach((key, value) -> exchange.getResponseHeaders().set(key, value));
        exchange.getResponseHeaders().set("X-Cache", isFromCache ? "HIT" : "MISS");
        try {
            exchange.sendResponseHeaders(response.getResponseCode(), response.getBody().length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBody());
            outputStream.close();
            logger.info("Successfully sent the response back to the client!\nResponseHeader(X-Cache): {}",
                    exchange.getResponseHeaders().get("X-Cache"));
        }
        catch (IOException e){
            logger.error("Can't send response back to the client: {}", e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, String message) {
        byte[] body = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        try {
            exchange.sendResponseHeaders(500, body.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(body);
            outputStream.close();
            logger.info("Successfully sent the error back to the client!");
        } catch (IOException e) {
            logger.error("Can't send error back to the client: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stopServer() {
        if (proxyServer != null) {
            proxyServer.stop(0); // Stop immediately
            proxyServer = null;
            logger.info("Caching proxy server stopped.");
        }
    }

    public void clearCache() {
        cache = new ConcurrentHashMap<>();
        logger.info("Cache was cleared.");
    }
}
