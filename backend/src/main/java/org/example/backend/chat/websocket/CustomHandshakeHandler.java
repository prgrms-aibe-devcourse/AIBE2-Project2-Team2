package org.example.backend.chat.websocket;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Log4j2
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        //  JwtHandshakeInterceptor에서 저장한 userEmail을 꺼내기
        String email = (String) attributes.get("userEmail");

        if (email != null) {
            log.info("✅ Handshake에서 Principal 등록: {}", email);
            return new StompPrincipal(email);
        } else {
            log.warn("❌ Handshake에서 userEmail 없음 → Principal null");
            return null;
        }
    }
}