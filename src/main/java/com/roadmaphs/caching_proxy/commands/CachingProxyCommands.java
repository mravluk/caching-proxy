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
            @ShellOption(value = "--origin", defaultValue = ShellOption.NULL, arity = 1) String origin,
            @ShellOption(value = "--clear-cache", defaultValue = ShellOption.NULL, arity = 0) Boolean clearCache) {

        if (clearCache != null && port == null && origin == null) {
            if (httpServer != null) {
                httpServerCreator.clearCache();
                return "Clearing the cache..";
            } else
                return "ERROR: Can not clear the cache as the server isn't created/started!";
        } else if (clearCache == null && port != null && origin != null) {
            if (httpServer == null)
                httpServer = httpServerCreator.startProxyServer(port, origin);
            else
                return String.format("Can't create another server as one already exists at %s", httpServer.getAddress());
            return String.format("This is caching proxy command that creates a proxy server %s on port %d.",
                    origin, port);
        } else if (clearCache == null && port == null && origin == null) {
            return "Current options for the use of caching proxy:\nCreating proxy server at <origin>:<port>: --port " +
                    "<port> --origin <origin>\nClearing the cache: --clear-cache";
        } else
            return "ERROR: Nothing is done. Use either --clear-cache or --port and --origin instead.";
    }
}
