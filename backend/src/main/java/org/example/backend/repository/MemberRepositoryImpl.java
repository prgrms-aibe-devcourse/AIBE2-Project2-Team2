package org.example.backend.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.QMember;
import org.example.backend.mypage.dto.response.MyPageResponseDto;

@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public MyPageResponseDto findMyPageInfoByEmail(String email) {
        log.info("리포지토리 시작 - 이메일: {}", email);

        QMember member = QMember.member;

        try {
            MyPageResponseDto result = queryFactory
                    .select(Projections.constructor(MyPageResponseDto.class,
                            member.nickname,
                            member.profileImageUrl,
                            member.email,
                            member.joinType,
                            member.phone
                    ))
                    .from(member)
                    .where(member.email.eq(email))
                    .fetchOne();

            log.info("QueryDSL 결과: {}", result);
            return result;
        } catch (Exception e) {
            log.error("리포지토리 오류 발생 - 이메일: {}, 오류: {}", email, e.getMessage(), e);
            throw e;
        }
    }
}
