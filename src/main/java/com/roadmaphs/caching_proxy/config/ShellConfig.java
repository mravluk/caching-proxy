package com.roadmaphs.caching_proxy.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.standard.ShellComponent;

@Configuration
@ComponentScan(basePackages = "com.roadmaphs.caching_proxy.commands")
public class ShellConfig {
}
