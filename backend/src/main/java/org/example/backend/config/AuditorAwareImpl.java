package org.example.backend.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;
import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        //현재 로그인한 사용자의 인증 정보 (Spring Security의 SecurityContext에서 가져옴)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "";
        if(authentication != null){
            userId = authentication.getName(); // 현재 로그인 한 사용자의 정보를 조회하여 사용자의 이름을 등록자와 수정자로 지정
        }
        return Optional.of(userId); //null을 방지하기 위한 Java Optional 사용 (JPA 감사 필드에 값 자동 설정)
    }
}
