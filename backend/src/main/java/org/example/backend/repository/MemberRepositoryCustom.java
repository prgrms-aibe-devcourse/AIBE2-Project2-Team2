package org.example.backend.repository;

import org.example.backend.mypage.dto.response.MyPageResponseDto;

public interface MemberRepositoryCustom {
    MyPageResponseDto findMyPageInfoByEmail(String email);

}
