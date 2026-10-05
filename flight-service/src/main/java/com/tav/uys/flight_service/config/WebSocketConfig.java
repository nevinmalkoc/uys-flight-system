package com.tav.uys.flight_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // İstemcilerin dinleyeceği prefix (Örn: /topic/flights)
        config.enableSimpleBroker("/topic");
        // İstemciden sunucuya gönderilecek mesajların prefix'i
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend'in bağlanacağı handshake endpoint'i
        registry.addEndpoint("/ws-uys")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}