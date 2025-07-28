package org.example.backend.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryExpertDto;
import org.example.backend.entity.*;
import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class MatchingRepositoryImpl implements MatchingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 전문가 이메일과 검색 조건, 페이징 정보를 받아서
     * 해당 전문가가 참여한 매칭 이력 목록을 조회한다.
     *
     * 주요 조회 대상 필드:
     * - 매칭 ID
     * - 콘텐츠 제목 및 썸네일 이미지 (썸네일은 콘텐츠 이미지 중 orderIndex 0번인 이미지)
     * - 전문가 닉네임, 프로필 이미지 URL, 전화번호
     * - 매칭 상태, 작업 시작일, 작업 종료일
     * - 견적서 총 금액
     * - 견적서에 포함된 고른 상품 리스트 (상품명, 가격)
     *
     * 조건 필터링:
     * - 매칭 상태 (MatchingStatus)
     * - 매칭 ID (특정 매칭 단일 조회)
     * - 작업 시작일 기준 포함 시작월과 종료월 (YearMonth)
     * - 매칭 상대방 닉네임 (매칭 요청자 닉네임 포함여부, 대소문자 무시)
     *
     * 쿼리는 QueryDSL 사용, 페이징 적용하여 결과 제한
     *
     * 중복 매칭ID에 대해 상품 리스트를 하나의 DTO에 묶기 위해 LinkedHashMap으로 관리한다.
     *
     * @param expertEmail 로그인한 전문가 이메일
     * @param condition 매칭 검색 조건 객체
     * @param pageable 페이징 정보
     * @return 매칭 이력 목록 DTO 리스트
     */
    @Override
    public Page<MatchingSummaryUserDto> findExpertMatchingSummaries(String expertEmail, MatchingSearchCondition condition, Pageable pageable) {
        QMatching matching = QMatching.matching;
        QContent content = QContent.content;
        QMember expert = QMember.member;
        QContentImage contentImage = QContentImage.contentImage;
        QEstimateRecord estimateRecord = QEstimateRecord.estimateRecord;
        QSelectedProduct selectedProduct = QSelectedProduct.selectedProduct;

        // 썸네일 이미지 서브쿼리
        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(contentImage.imageUrl)
                .from(contentImage)
                .where(contentImage.content.eq(content)
                        .and(contentImage.orderIndex.eq((byte) 0)))
                .limit(1);

        // 조건절 정의 (BooleanBuilder 사용)
        BooleanBuilder where = new BooleanBuilder()
                .and(expert.email.eq(expertEmail))
                .and(condition.getMatchingStatus() != null ? matching.status.eq(condition.getMatchingStatus()) : null)
                .and(condition.getMatchingId() != null ? matching.matchingId.eq(condition.getMatchingId()) : null)
                .and(condition.getFromMonth() != null ? matching.startDate.goe(condition.getFromMonth().atDay(1)) : null)
                .and(condition.getToMonth() != null ? matching.startDate.loe(condition.getToMonth().atEndOfMonth()) : null)
                .and(StringUtils.hasText(condition.getNickname()) ? matching.member.nickname.containsIgnoreCase(condition.getNickname()) : null);

        // 데이터 쿼리
        JPQLQuery<Tuple> query = queryFactory
                .select(
                        matching.matchingId,
                        content.title,
                        thumbnailSubQuery,
                        matching.member.nickname,
                        matching.member.phone,
                        matching.status,
                        matching.startDate,
                        matching.endDate,
                        matching.regTime,
                        estimateRecord.totalPrice,
                        selectedProduct.name,
                        selectedProduct.price
                )
                .from(matching)
                .join(matching.content, content)
                .join(content.member, expert)
                .leftJoin(matching.estimateRecord, estimateRecord)
                .leftJoin(estimateRecord.selectedProducts, selectedProduct)
                .where(where)
                .orderBy(matching.matchingId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Tuple> tuples = query.fetch();

        // total count 쿼리
        Long totalCount = queryFactory
                .select(matching.count())
                .from(matching)
                .join(matching.content, content)
                .join(content.member, expert)
                .where(where)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        Map<Long, MatchingSummaryUserDto> dtoMap = new LinkedHashMap<>();

        for (Tuple t : tuples) {
            Long matchingId = t.get(matching.matchingId);
            MatchingSummaryUserDto dto = dtoMap.get(matchingId);

            Long totalPriceLong = t.get(estimateRecord.totalPrice);

            if (dto == null) {
                dto = MatchingSummaryUserDto.builder()
                        .matchingId(matchingId)
                        .contentTitle(t.get(content.title))
                        .contentThumbnailUrl(t.get(thumbnailSubQuery))
                        .userName(t.get(matching.member.nickname))
                        .userPhone(t.get(matching.member.phone))
                        .matchingStatus(t.get(matching.status))
                        .workStartDate(t.get(matching.startDate))
                        .workEndDate(t.get(matching.endDate))
                        .regTime(t.get(matching.regTime))
                        .totalPrice(totalPriceLong != null ? totalPriceLong.intValue() : null)
                        .selectedItems(new ArrayList<>())
                        .build();
                dtoMap.put(matchingId, dto);
            }

            String productName = t.get(selectedProduct.name);
            Long productPrice = t.get(selectedProduct.price);
            if (productName != null && productPrice != null) {
                dto.getSelectedItems().add(
                        new MatchingSummaryUserDto.SelectedItemDto(productName, productPrice.intValue())
                );
            }
        }

        List<MatchingSummaryUserDto> contentList = new ArrayList<>(dtoMap.values());

        return new PageImpl<>(contentList, pageable, total);
    }


    public Page<MatchingSummaryExpertDto> findUserMatchingSummaries(String userEmail, MatchingSearchCondition condition, Pageable pageable) {
        QMatching matching = QMatching.matching;
        QContent content = QContent.content;
        QMember user = QMember.member;
        QMember expert = new QMember("expert");
        QContentImage contentImage = QContentImage.contentImage;
        QEstimateRecord estimateRecord = QEstimateRecord.estimateRecord;
        QSelectedProduct selectedProduct = QSelectedProduct.selectedProduct;
        QReview review = QReview.review;

        // 썸네일 이미지 서브쿼리
        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(contentImage.imageUrl)
                .from(contentImage)
                .where(contentImage.content.eq(content)
                        .and(contentImage.orderIndex.eq((byte) 0)))
                .limit(1);

        // 조건 추출
        BooleanBuilder where = new BooleanBuilder()
                .and(user.email.eq(userEmail))
                .and(condition.getMatchingStatus() != null ? matching.status.eq(condition.getMatchingStatus()) : null)
                .and(condition.getMatchingId() != null ? matching.matchingId.eq(condition.getMatchingId()) : null)
                .and(condition.getFromMonth() != null ? matching.startDate.goe(condition.getFromMonth().atDay(1)) : null)
                .and(condition.getToMonth() != null ? matching.startDate.loe(condition.getToMonth().atEndOfMonth()) : null)
                .and(StringUtils.hasText(condition.getNickname()) ?
                        content.member.nickname.containsIgnoreCase(condition.getNickname()) : null);

        // 데이터 쿼리 (리뷰 정보 추가)
        JPQLQuery<Tuple> query = queryFactory
                .select(
                        matching.matchingId,
                        content.title,
                        thumbnailSubQuery,
                        expert.nickname,
                        expert.phone,
                        matching.status,
                        matching.startDate,
                        matching.endDate,
                        matching.regTime,
                        estimateRecord.totalPrice,
                        selectedProduct.name,
                        selectedProduct.price,
                        review.reviewId.isNotNull() // 리뷰 존재 여부
                )
                .from(matching)
                .join(matching.member, user)
                .join(matching.content, content)
                .join(content.member, expert)
                .leftJoin(matching.estimateRecord, estimateRecord)
                .leftJoin(estimateRecord.selectedProducts, selectedProduct)
                .leftJoin(matching.review, review) // 리뷰 테이블 조인
                .where(where)
                .orderBy(matching.matchingId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Tuple> tuples = query.fetch();

        // total count 쿼리
        Long totalCount = queryFactory
                .select(matching.count())
                .from(matching)
                .join(matching.member, user)
                .join(matching.content, content)
                .where(where)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        // 변환 로직
        Map<Long, MatchingSummaryExpertDto> dtoMap = new LinkedHashMap<>();
        for (Tuple t : tuples) {
            Long matchingId = t.get(matching.matchingId);
            MatchingSummaryExpertDto dto = dtoMap.get(matchingId);

            Long totalPriceLong = t.get(estimateRecord.totalPrice);
            Boolean hasReview = t.get(review.reviewId.isNotNull()); // 리뷰 존재 여부 가져오기

            if (dto == null) {
                dto = MatchingSummaryExpertDto.builder()
                        .matchingId(matchingId)
                        .contentTitle(t.get(content.title))
                        .contentThumbnailUrl(t.get(thumbnailSubQuery))
                        .expertName(t.get(expert.nickname))
                        .expertPhone(t.get(expert.phone))
                        .matchingStatus(t.get(matching.status))
                        .workStartDate(t.get(matching.startDate))
                        .workEndDate(t.get(matching.endDate))
                        .regTime(t.get(matching.regTime))
                        .totalPrice(totalPriceLong != null ? totalPriceLong.intValue() : null)
                        .reviewed(hasReview != null && hasReview) // 리뷰 존재 여부 설정
                        .selectedItems(new ArrayList<>())
                        .build();
                dtoMap.put(matchingId, dto);
            }

            String itemName = t.get(selectedProduct.name);
            Long itemPrice = t.get(selectedProduct.price);
            if (itemName != null && itemPrice != null) {
                dto.getSelectedItems().add(
                        new MatchingSummaryExpertDto.SelectedItemDto(itemName, itemPrice.intValue())
                );
            }
        }

        List<MatchingSummaryExpertDto> contentList = new ArrayList<>(dtoMap.values());

        return new PageImpl<>(contentList, pageable, total);
    }


}