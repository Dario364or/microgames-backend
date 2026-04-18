package com.example.microgames_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura los prefijos del message broker.
     * - /topic  → mensajes broadcast (todos los jugadores de una sala)
     * - /queue  → mensajes privados (solo para un jugador, ej: tablero propio en Hundir la Flota)
     * - /app    → prefijo para mensajes enviados por el cliente al servidor
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Endpoint al que los clientes se conectan vía WebSocket.
     * SockJS como fallback para entornos que no soporten WS nativo.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/game-ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint nativo WS (para React Native con @stomp/stompjs)
        registry.addEndpoint("/game-ws")
                .setAllowedOriginPatterns("*");
    }
}
