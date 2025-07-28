package org.example.backend.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.search.dto.response.SearchContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.example.backend.entity.QContent.content;
import static org.example.backend.entity.QCategory.category;
import static org.example.backend.entity.QMember.member;
import static org.example.backend.entity.QContentImage.contentImage;
import static org.example.backend.entity.QExpertProfile.expertProfile;
import org.example.backend.constant.Status;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SearchContentResponse> findContentsByCategoryIds(List<Long> categoryIds, Pageable pageable) {
        log.info("쿼리 DSL 컨텐츠 검색 시작 - categoryIds count: {}", categoryIds.size());

        if (categoryIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        // 썸네일 이미지 서브쿼리 (thumbnail = true로 지정된 이미지)
        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(contentImage.imageUrl)
                .from(contentImage)
                .where(contentImage.content.eq(content)
                        .and(contentImage.thumbnail.eq(true)))
                .limit(1);

        // 데이터 쿼리
        JPQLQuery<Tuple> query = queryFactory
                .select(
                        category.name,
                        content.contentId,
                        content.title,
                        content.budget,
                        thumbnailSubQuery,
                        member.nickname,
                        expertProfile.reviewCount,
                        expertProfile.rating
                )
                .from(content)
                .join(content.category, category)
                .join(content.member, member)
                .leftJoin(member.expertProfile, expertProfile)
                .where(content.category.categoryId.in(categoryIds)
                        .and(content.status.eq(Status.ACTIVE)))
                .orderBy(content.updateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Tuple> tuples = query.fetch();

        // total count 쿼리
        Long totalCount = queryFactory
                .select(content.count())
                .from(content)
                .where(content.category.categoryId.in(categoryIds)
                        .and(content.status.eq(Status.ACTIVE)))
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        // DTO 변환
        List<SearchContentResponse> contentList = new ArrayList<>();
        for (Tuple tuple : tuples) {
            SearchContentResponse dto = new SearchContentResponse();
            dto.setCategoryName(tuple.get(category.name));
            dto.setContentId(tuple.get(content.contentId));
            dto.setTitle(tuple.get(content.title));
            dto.setBudget(tuple.get(content.budget));
            dto.setContentThumbnailUrl(tuple.get(thumbnailSubQuery));
            dto.setExpertName(tuple.get(member.nickname));

            // ExpertProfile에서 리뷰 수와 평점 가져오기
            Long reviewCountLong = tuple.get(expertProfile.reviewCount);
            dto.setReviewCount(reviewCountLong != null ? reviewCountLong : 0L);

            Double avgRating = tuple.get(expertProfile.rating);
            dto.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

            contentList.add(dto);
        }

        log.info("쿼리 DSL 컨텐츠 검색 완료 - 결과 수: {}", contentList.size());
        return new PageImpl<>(contentList, pageable, total);
    }

    @Override
    public Page<SearchContentResponse> findContentsByKeyword(String keyword, Pageable pageable) {
        log.info("쿼리 DSL 키워드 검색 시작 - keyword: '{}'", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        String trimmedKeyword = keyword.trim();

        // 썸네일 이미지 서브쿼리 (thumbnail = true로 지정된 이미지)
        JPQLQuery<String> thumbnailSubQuery = JPAExpressions
                .select(contentImage.imageUrl)
                .from(contentImage)
                .where(contentImage.content.eq(content)
                        .and(contentImage.thumbnail.eq(true)))
                .limit(1);

        // 데이터 쿼리
        JPQLQuery<Tuple> query = queryFactory
                .select(
                        category.name,
                        content.contentId,
                        content.title,
                        content.budget,
                        thumbnailSubQuery,
                        member.nickname,
                        expertProfile.reviewCount,
                        expertProfile.rating
                )
                .from(content)
                .join(content.category, category)
                .join(content.member, member)
                .leftJoin(member.expertProfile, expertProfile)
                .where((content.title.containsIgnoreCase(trimmedKeyword)
                        .or(content.description.containsIgnoreCase(trimmedKeyword)))
                        .and(content.status.eq(Status.ACTIVE)))
                .orderBy(content.updateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<Tuple> tuples = query.fetch();

        // total count 쿼리
        Long totalCount = queryFactory
                .select(content.count())
                .from(content)
                .where((content.title.containsIgnoreCase(trimmedKeyword)
                        .or(content.description.containsIgnoreCase(trimmedKeyword)))
                        .and(content.status.eq(Status.ACTIVE)))
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        // DTO 변환 (기존과 동일한 로직)
        List<SearchContentResponse> contentList = new ArrayList<>();
        for (Tuple tuple : tuples) {
            SearchContentResponse dto = new SearchContentResponse();
            dto.setCategoryName(tuple.get(category.name));
            dto.setContentId(tuple.get(content.contentId));
            dto.setTitle(tuple.get(content.title));
            dto.setBudget(tuple.get(content.budget));
            dto.setContentThumbnailUrl(tuple.get(thumbnailSubQuery));
            dto.setExpertName(tuple.get(member.nickname));

            // ExpertProfile에서 리뷰 수와 평점 가져오기
            Long reviewCountLong = tuple.get(expertProfile.reviewCount);
            dto.setReviewCount(reviewCountLong != null ? reviewCountLong : 0L);

            Double avgRating = tuple.get(expertProfile.rating);
            dto.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

            contentList.add(dto);
        }

        log.info("쿼리 DSL 키워드 검색 완료 - 결과 수: {}", contentList.size());
        return new PageImpl<>(contentList, pageable, total);
    }
}