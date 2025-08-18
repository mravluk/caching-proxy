package com.roadmaphs.caching_proxy.commands;

import com.roadmaphs.caching_proxy.server.ProxyServerCreator;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent()
public class CachingProxyCommands {

    private static final Logger logger = LoggerFactory.getLogger(CachingProxyCommands.class);
    private HttpServer proxyServer;
    private final UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);

    @Autowired
    private ProxyServerCreator proxyServerCreator;

    @ShellMethod(value = "To create a proxy server and clear cache.")
    public String cachingProxy(
            @ShellOption(value = "--port", defaultValue = ShellOption.NULL, arity = 1) Integer port,
            @ShellOption(value = "--origin", defaultValue = ShellOption.NULL, arity = 1) String origin,
            @ShellOption(value = "--clear-cache", defaultValue = ShellOption.NULL, arity = 0) Boolean clearCache) {

        String commandResponse;
        if (clearCache != null && port == null && origin == null) {
            if (proxyServer != null) {
                proxyServerCreator.clearCache();
                logger.info("Successfully cleared the cache.");
                commandResponse = "Cleared the cache.";
            } else {
                logger.error("Can not clear the cache as the server isn't created/started!");
                commandResponse = "ERROR: Can not clear the cache as the server isn't created/started!";
            }
        } else if (clearCache == null && port != null && origin != null) {
            if (port < 0 || port > 65535) {
                logger.error("ERROR: Port number must be between 0 and 65535!");
                commandResponse = "ERROR: Port number must be between 0 and 65535!";
            } else if (!urlValidator.isValid(origin)) {
                logger.error("ERROR: Origin address is not valid!");
                commandResponse = "ERROR: Origin address is not valid!";
            } else if (proxyServer == null) {
                proxyServer = proxyServerCreator.startProxyServer(port, origin);
                commandResponse = String.format("This is caching proxy command that creates a proxy server %s on " +
                        "port %d.", origin, port);
            } else {
                logger.warn("Can't create a server as one already exists at {}", proxyServer.getAddress());
                commandResponse = String.format("Can't create a server as one already exists at %s",
                        proxyServer.getAddress());
            }
        } else if (clearCache == null && port == null && origin == null) {
            commandResponse = "Current options for the use of caching proxy:\nCreating proxy server: " +
                    "\n\t--port <port> --origin <origin>\nClearing the cache:\n\t--clear-cache";
        } else {
            logger.error("ERROR: Nothing is done. Use either --clear-cache or --port and --origin instead.");
            commandResponse = "ERROR: Nothing is done. Use either --clear-cache or --port and --origin instead.";
        }
        return commandResponse;
    }
}
