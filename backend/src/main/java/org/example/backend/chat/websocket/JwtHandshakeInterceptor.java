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

import javax.servlet.http.Cookie;
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

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        // ✅ 1) 먼저 쿠키에서 토큰 확인
        String jwt = null;
        if (servletRequest.getCookies() != null) {
            for (Cookie cookie : servletRequest.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // ✅ 2) 쿠키가 없으면 쿼리 파라미터에서 token 추출
        if (jwt == null) {
            jwt = servletRequest.getParameter("token");
        }

        if (jwt != null && jwtUtil.validateToken(jwt)) {
            String email = jwtUtil.getUsername(jwt);
            attributes.put("memberEmail", email);
            log.info("✅ WebSocket 인증 성공 (email={})", email);
            return true;
        }

        log.warn("❌ WebSocket 인증 실패 (쿠키/쿼리 파라미터 없음 or 검증 실패)");
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