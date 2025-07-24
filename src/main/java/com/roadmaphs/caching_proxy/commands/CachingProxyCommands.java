package com.roadmaphs.caching_proxy.commands;

import com.roadmaphs.caching_proxy.server.HttpServerCreator;
import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent()
public class CachingProxyCommands {

    private HttpServer httpServer;

    @Autowired
    private HttpServerCreator httpServerCreator;

    @ShellMethod(value = "To create a proxy server and clear cache.")
    public String cachingProxy(
            @ShellOption(value = "--port", defaultValue = ShellOption.NULL, arity = 1) Integer port,
            @ShellOption(value = "--server", defaultValue = ShellOption.NULL, arity = 1) String server,
            @ShellOption(value = "--clear-cache", defaultValue = ShellOption.NULL, arity = 0) Boolean clearCache){

        if (clearCache != null && port == null && server == null){
            return "Clearing the cache..";
        }

        else if (clearCache == null && port != null && server != null) {
            httpServer = httpServerCreator.startProxyServer(port, server);
            return String.format("This is caching proxy command that creates a proxy server %s on port %d.",
                    server, port);
        }
        else
            return "ERROR: Nothing is done. Use either --clear-cache or --port and --server instead.";
    }
}
