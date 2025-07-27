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
import static org.example.backend.entity.QContent.content;
import static org.example.backend.entity.QReviewImage.reviewImage;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Review> findReviewsByExpertMemberId(Long expertMemberId, Pageable pageable) {
        // 리뷰 목록 조회 쿼리 - 필요한 모든 연관 엔티티를 fetchJoin으로 한번에 가져옴
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .join(review.matching, matching).fetchJoin()
                .join(matching.content, content).fetchJoin()
                .join(content.member, member).fetchJoin() // 전문가 정보
                .join(matching.member, QMember.member).fetchJoin() // 리뷰 작성자 정보
                .leftJoin(review.reviewImage, reviewImage).fetchJoin() // 리뷰 이미지 (선택적)
                .where(
                        expertMemberIdEq(expertMemberId),
                        statusActive()
                )
                .orderBy(review.regTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회 쿼리 (fetchJoin 없이 count만)
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .join(review.matching, matching)
                .join(matching.content, content)
                .where(
                        expertMemberIdEq(expertMemberId),
                        statusActive()
                );

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 전문가 멤버 ID로 상세 정보와 함께 리뷰 조회
     */
    public Page<Review> findReviewsByExpertMemberIdWithDetails(Long expertMemberId, Status status, Pageable pageable) {
        // 리뷰 목록 조회 - 모든 필요한 연관 엔티티를 한번에 가져옴
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .join(review.matching, matching).fetchJoin()
                .join(matching.member, QMember.member).fetchJoin() // 리뷰 작성자
                .leftJoin(review.reviewImage, reviewImage).fetchJoin() // 리뷰 이미지
                .where(
                        matching.content.member.memberId.eq(expertMemberId),
                        review.status.eq(status)
                )
                .orderBy(review.regTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (fetchJoin 없이)
        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .join(review.matching, matching)
                .where(
                        matching.content.member.memberId.eq(expertMemberId),
                        review.status.eq(status)
                );

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    /**
     * 전문가 회원 ID 조건
     */
    private BooleanExpression expertMemberIdEq(Long expertMemberId) {
        return expertMemberId != null ? content.member.memberId.eq(expertMemberId) : null;
    }

    /**
     * 활성 상태 조건
     */
    private BooleanExpression statusActive() {
        return review.status.eq(Status.ACTIVE);
    }
}