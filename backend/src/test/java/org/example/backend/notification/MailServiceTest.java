package org.example.backend.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MailService 단위 테스트
 */
@SpringBootTest
class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    void sendSimpleMailTest() {

        mailService.sendSimpleMail(
                "",    // ← 여기 본인 이메일 입력!
                "테스트 메일입니다",
                "Spring에서 보낸 테스트 메일입니다."
        );
    }
}
