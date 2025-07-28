package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import org.example.backend.chat.websocket.CustomHandshakeHandler;
import org.example.backend.chat.websocket.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");        // ✅ 구독 prefix
        registry.setApplicationDestinationPrefixes("/pub"); // ✅ 송신 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Native WebSocket 이므로 withSockJS() 제거
        registry.addEndpoint("/ws/chat")
                .addInterceptors(jwtHandshakeInterceptor)  // ✅ JWT 토큰 검증 인터셉터
                .setHandshakeHandler(new CustomHandshakeHandler()) // ✅ Principal 등록
                .setAllowedOriginPatterns("*");            // ✅ CORS 허용
    }
}