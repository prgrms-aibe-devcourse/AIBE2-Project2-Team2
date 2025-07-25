package org.example.backend.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 전담 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * 지정된 이메일 주소로 간단한 텍스트 메일을 발송합니다.
     *
     * @param to      수신자 이메일 주소
     * @param subject 메일 제목
     * @param text    메일 본문(텍스트)
     */
    public void sendSimpleMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("이메일 전송 완료: {}", to);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", to, e);
        }
    }
}
