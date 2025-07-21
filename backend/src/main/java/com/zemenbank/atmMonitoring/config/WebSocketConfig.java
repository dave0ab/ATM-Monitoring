package com.zemenbank.atmMonitoring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Value("${allowed.origin}")
    private String allowedOrigins;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        logger.info("Configuring message broker...");

        config.enableSimpleBroker("/topic");  // Defines message broker prefix
        config.setApplicationDestinationPrefixes("/app");  // Prefix for messages from client

        logger.info("Message broker configured successfully.");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("Registering WebSocket STOMP endpoint...");

        registry.addEndpoint("/ws")  // Define WebSocket endpoint without SockJS
                .setAllowedOrigins(allowedOrigins);  // Allow cross-origin requests

        logger.info("WebSocket endpoint '/ws' registered successfully.");
    }
}
