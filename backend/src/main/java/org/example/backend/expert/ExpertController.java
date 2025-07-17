package org.example.backend.expert;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.expert.dto.ExpertRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expert")
@Tag(name = "Expert", description = "전문가 관련 API")
public class ExpertController {
    private final ExpertService expertService;

    /**
     * 일반유저에서 전문가로 전환하는 API
     * 전문가로 전환하기 위해서는 일반 사용자 계정이 필요합니다.
     * 전문가로 전환하기 위해서는 specialties와 detailFields를 포함한 요청이 필요합니다.
     */
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeExpert(
            @Valid ExpertRequestDto expertRequestDto,
            Principal principal
    ){
        String email = principal.getName();
        log.info("전문가 전환 요청: {}", expertRequestDto);
        expertService.upgradeToExpert(email, expertRequestDto);
        return ResponseEntity.ok("전문가로 전환되었습니다.");
    }
}
