package org.example.backend.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.constant.Status;
import org.example.backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.backend.entity.QReview.review;
import static org.example.backend.entity.QMatching.matching;
import static org.example.backend.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Review> findReviewsByExpertMemberId(Long expertMemberId, Pageable pageable) {

        // 리뷰 목록 조회 쿼리
        // Review -> Matching -> Content -> Member(전문가) 경로로 조회
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .join(review.matching, matching).fetchJoin()
                .join(matching.content, QContent.content).fetchJoin()
                .join(QContent.content.member, member).fetchJoin() // Content의 member가 전문가
                .join(matching.member, QMember.member).fetchJoin() // Matching의 member가 의뢰인 (리뷰 작성자)
                .where(
                        expertMemberIdEq(expertMemberId),
                        statusActive()
                )
                .orderBy(review.regTime.desc()) // BaseTimeEntity의 regTime 필드로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회 쿼리 (성능 최적화를 위해 별도 쿼리)
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .join(review.matching, matching)
                .join(matching.content, QContent.content)
                .where(
                        expertMemberIdEq(expertMemberId),
                        statusActive()
                );

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 전문가 회원 ID 조건
     * Content의 member가 전문가이므로 matching.content.member.memberId로 조회
     */
    private BooleanExpression expertMemberIdEq(Long expertMemberId) {
        return expertMemberId != null ? matching.content.member.memberId.eq(expertMemberId) : null;
    }

    /**
     * 활성 상태 조건
     */
    private BooleanExpression statusActive() {
        return review.status.eq(Status.ACTIVE);
    }
}