package org.example.backend.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.backend.jwt.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");

            if (token == null || !jwtUtil.validateToken(token)) {
                log.warn("❌ WebSocket 인증 실패");
                return false;
            }

            String email = jwtUtil.getUsername(token);
            attributes.put("userEmail", email);
            log.info("✅ WebSocket 인증 성공: {}", email);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        log.debug("WebSocket 핸드셰이크 완료");
    }
}