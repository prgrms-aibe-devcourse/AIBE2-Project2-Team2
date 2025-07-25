package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backend.entity.Member;

@Getter
@AllArgsConstructor
public class MemberDto {

    private Long memberId;
    private String nickname;
    private String email;

    public static MemberDto from(Member m) {
        return new MemberDto(
                m.getMemberId(),
                m.getNickname(),
                m.getEmail()
        );
    }
}