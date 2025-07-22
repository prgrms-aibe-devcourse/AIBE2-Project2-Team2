package org.example.backend.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryExpertDto;
import org.example.backend.entity.*;
import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
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
    public List<MatchingSummaryUserDto> findExpertMatchingSummaries(String expertEmail, MatchingSearchCondition condition, Pageable pageable) {
        QMatching matching = QMatching.matching;
        QContent content = QContent.content;
        QMember expert = QMember.member;
        QContentImage contentImage = QContentImage.contentImage;
        QEstimateRecord estimateRecord = QEstimateRecord.estimateRecord;
        QSelectedProduct selectedProduct = QSelectedProduct.selectedProduct;

        // 콘텐츠 썸네일 이미지를 서브쿼리로 조회한다.
        // 콘텐츠 이미지 중 orderIndex가 0인 첫 번째 이미지를 대표 이미지로 사용한다.
        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(contentImage.imageUrl)
                .from(contentImage)
                .where(contentImage.content.eq(content)
                        .and(contentImage.orderIndex.eq((byte) 0)))
                .limit(1);

        // QueryDSL JPQLQuery 생성
        // 여러 테이블 Join 및 조건 필터링 수행
        JPQLQuery<Tuple> query = queryFactory
                .select(
                        matching.matchingId,           // 매칭 고유 ID
                        content.title,                 // 콘텐츠 제목
                        thumbnailSubQuery,             // 썸네일 이미지 URL (서브쿼리)
                        matching.member.nickname,      // 일반 사용자 닉네임
                        matching.member.phone,         // 일반 사용자 전화번호
                        matching.status,               // 매칭 상태
                        matching.startDate,            // 작업 시작일
                        matching.endDate,              // 작업 종료일
                        estimateRecord.totalPrice,     // 견적서 총 금액
                        selectedProduct.name,          // 고른 상품명
                        selectedProduct.price          // 고른 상품 가격
                )
                .from(matching)
                .join(matching.content, content)             // 매칭 -> 콘텐츠 (다대일)
                .join(content.member, expert)                 // 콘텐츠 -> 전문가 (다대일)
                .leftJoin(matching.estimateRecord, estimateRecord)  // 매칭 -> 견적서 (일대일, optional)
                .leftJoin(estimateRecord.selectedProducts, selectedProduct)  // 견적서 -> 선택 상품 (일대다)
                .where(
                        expert.email.eq(expertEmail)    // 전문가 이메일 일치 조건
                                // 매칭 상태 필터링 조건 (있으면 적용)
                                .and(condition.getMatchingStatus() != null ? matching.status.eq(condition.getMatchingStatus()) : null)
                                // 특정 매칭 ID 필터링 조건 (있으면 적용)
                                .and(condition.getMatchingId() != null ? matching.matchingId.eq(condition.getMatchingId()) : null)
                                // 작업 시작일 >= fromMonth 1일 (있으면 적용)
                                .and(condition.getFromMonth() != null ? matching.startDate.goe(condition.getFromMonth().atDay(1)) : null)
                                // 작업 시작일 <= toMonth 말일 (있으면 적용)
                                .and(condition.getToMonth() != null ? matching.startDate.loe(condition.getToMonth().atEndOfMonth()) : null)
                                // 매칭 상대방 닉네임 포함 조건 (대소문자 무시)
                                .and(StringUtils.hasText(condition.getNickname()) ? matching.member.nickname.containsIgnoreCase(condition.getNickname()) : null)
                );

        // 정렬 조건 반영
        query.orderBy(matching.matchingId.desc())  // 최신 등록순 고정
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 쿼리 실행 후 결과 리스트 획득 (Tuple 리스트)
        List<Tuple> tuples = query.fetch();

        // 중복된 matchingId를 가진 행들이 있을 수 있으므로
        // 매칭 ID 기준으로 DTO를 관리하기 위해 LinkedHashMap 사용
        Map<Long, MatchingSummaryUserDto> dtoMap = new LinkedHashMap<>();

        for (Tuple t : tuples) {
            Long matchingId = t.get(matching.matchingId);
            MatchingSummaryUserDto dto = dtoMap.get(matchingId);

            // 견적서 총 금액은 Long 타입이며 null일 수 있으므로 안전하게 변환
            Long totalPriceLong = t.get(estimateRecord.totalPrice);

            // 매칭 ID에 해당하는 DTO가 아직 없으면 새로 생성하여 맵에 저장
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
                        .totalPrice(totalPriceLong != null ? totalPriceLong.intValue() : null)  // null 체크 후 int 변환
                        .selectedItems(new ArrayList<>())  // 고른 상품 리스트 초기화
                        .build();
                dtoMap.put(matchingId, dto);
            }

            // 현재 튜플에서 고른 상품명과 가격 정보 추출
            String productName = t.get(selectedProduct.name);
            Long productPrice = t.get(selectedProduct.price);

            // 상품 정보가 존재하면 DTO의 selectedItems 리스트에 추가
            if (productName != null && productPrice != null) {
                dto.getSelectedItems().add(
                        new MatchingSummaryUserDto.SelectedItemDto(productName, productPrice.intValue())
                );
            }
        }

        // 맵에 저장된 DTO들을 리스트로 변환하여 반환
        return new ArrayList<>(dtoMap.values());
    }

    public List<MatchingSummaryExpertDto> findUserMatchingSummaries(String userEmail, MatchingSearchCondition condition, Pageable pageable) {
        QMatching matching = QMatching.matching;
        QContent content = QContent.content;
        QMember user = QMember.member;
        QMember expert = new QMember("expert");
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
                        estimateRecord.totalPrice,
                        selectedProduct.name,
                        selectedProduct.price
                )
                .from(matching)
                .join(matching.member, user)  // 의뢰인 정보
                .join(matching.content, content)  // 매칭 -> 콘텐츠
                .join(content.member, expert)
                .leftJoin(matching.estimateRecord, estimateRecord)
                .leftJoin(estimateRecord.selectedProducts, selectedProduct)
                .where(
                        user.email.eq(userEmail)
                                .and(condition.getMatchingStatus() != null ? matching.status.eq(condition.getMatchingStatus()) : null)
                                .and(condition.getMatchingId() != null ? matching.matchingId.eq(condition.getMatchingId()) : null)
                                .and(condition.getFromMonth() != null ? matching.startDate.goe(condition.getFromMonth().atDay(1)) : null)
                                .and(condition.getToMonth() != null ? matching.startDate.loe(condition.getToMonth().atEndOfMonth()) : null)
                                .and(StringUtils.hasText(condition.getNickname()) ?
                                        content.member.nickname.containsIgnoreCase(condition.getNickname()) : null) // 전문가 닉네임
                )
                .orderBy(matching.matchingId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Tuple> tuples = query.fetch();
        Map<Long, MatchingSummaryExpertDto> dtoMap = new LinkedHashMap<>();

        for (Tuple t : tuples) {
            Long matchingId = t.get(matching.matchingId);
            MatchingSummaryExpertDto dto = dtoMap.get(matchingId);

            Long totalPriceLong = t.get(estimateRecord.totalPrice);

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
                        .totalPrice(totalPriceLong != null ? totalPriceLong.intValue() : null)
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

        return new ArrayList<>(dtoMap.values());
    }

}