package org.example.backend.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component  // 자동 등록되므로 FilterRegistrationBean 불필요
public class LoggerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청과 응답을 캐싱 래퍼로 감쌈
        var req = new ContentCachingRequestWrapper(request);
        var res = new ContentCachingResponseWrapper(response);

        // 실제 필터 체인 실행
        filterChain.doFilter(req, res);

        // 로그 출력 정보
        String method = req.getMethod();
        String uri = req.getRequestURI();
        int status = res.getStatus();

        String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8).trim();
        String resBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8).trim();

        log.info(
                "\n=================  [REQUEST] =================\n" +
                        "▶ Method : {}\n" +
                        "▶ URI    : {}\n" +
                        "▶ Body   : {}\n" +
                        "==============================================",
                method, uri, reqBody.isEmpty() ? "(no body)" : reqBody
        );

        log.info(
                "\n=================  [RESPONSE] ================\n" +
                        "◀ Status : {}\n" +
                        "◀ Body   : {}\n" +
                        "==============================================",
                status, resBody.isEmpty() ? "(no body)" : resBody
        );

        // response body를 실제로 클라이언트에 복사
        res.copyBodyToResponse();
    }
}
