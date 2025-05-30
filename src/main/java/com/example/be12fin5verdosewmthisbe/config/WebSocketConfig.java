package com.example.be12fin5verdosewmthisbe.config;

import com.example.be12fin5verdosewmthisbe.notification.AuthHandshakeInterceptor;
import com.example.be12fin5verdosewmthisbe.notification.CustomWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler handler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(
            CustomWebSocketHandler handler,
            AuthHandshakeInterceptor authHandshakeInterceptor,
            @Value("${websocket.allowed-origins}") String[] allowedOrigins
    ) {
        this.handler = handler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
