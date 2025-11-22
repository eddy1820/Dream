package com.eddy.dream.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Startup Listener
 * Prints useful information when the application starts
 */
@Slf4j
@Component
public class StartupListener {
    
    private final Environment environment;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerPath;
    
    public StartupListener(Environment environment) {
        this.environment = environment;
    }
    
    /**
     * Triggered when the application is fully started and ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String protocol = "http";
        String host = "localhost";
        
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Unable to determine host address, using 'localhost'");
        }
        
        log.info("\n" +
            "========================================================================================================\n" +
            "  🚀 Application '{}' is running!\n" +
            "========================================================================================================\n" +
            "  📋 Swagger UI:        {}://localhost:{}{}\n" +
            "  🌐 Local URL:         {}://localhost:{}\n" +
            "  🖥️  External URL:      {}://{}:{}\n" +
            "  📄 API Docs (JSON):   {}://localhost:{}/v3/api-docs\n" +
            "  ❤️  Health Check:      {}://localhost:{}/actuator/health\n" +
            "========================================================================================================",
            environment.getProperty("spring.application.name", "Dream"),
            protocol, serverPort, swaggerPath,
            protocol, serverPort,
            protocol, host, serverPort,
            protocol, serverPort,
            protocol, serverPort
        );
    }
}

