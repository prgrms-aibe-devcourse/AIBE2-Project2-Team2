package org.example.backend.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Content;
import org.example.backend.entity.ContentImage;
import org.example.backend.firebase.FirebaseImageService;
import org.example.backend.repository.ContentImageRepository;
import org.example.backend.repository.ContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentImageService {

    private final ContentImageRepository contentImageRepository;
    private final ContentRepository contentRepository;
    private final FirebaseImageService firebaseImageService; // FirebaseImageService 주입

    // 이미지 업로드 및 ContentImage 저장
    public String uploadContentImage(Long contentId, MultipartFile file, byte orderIndex) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));

        // FirebaseImageService를 사용하여 이미지 업로드
        String imageUrl = firebaseImageService.uploadImage(file, "content/" + contentId);

        // DB에 ContentImage 저장
        ContentImage contentImage = new ContentImage(content, imageUrl, orderIndex);
        contentImageRepository.save(contentImage);

        return imageUrl;
    }

    // 이미지 삭제
    public void deleteContentImage(Long contentImageId) {
        ContentImage contentImage = contentImageRepository.findById(contentImageId)
                .orElseThrow(() -> new IllegalArgumentException("ContentImage not found"));
        String imageUrl = contentImage.getImageUrl();

        // FirebaseImageService를 사용하여 이미지 삭제
        firebaseImageService.deleteImage(imageUrl);

        contentImageRepository.delete(contentImage);
    }

    // 여러 장의 이미지와 썸네일을 한 번에 업로드 및 저장
    public void uploadContentImagesBatch(Long contentId, List<MultipartFile> images, MultipartFile thumbnailImage) {
        if (thumbnailImage == null) {
            throw new IllegalArgumentException("썸네일 이미지를 반드시 전송해야 합니다.");
        }
        if (images != null && images.size() > 5) {
            throw new IllegalArgumentException("상세 이미지는 최대 5개까지 업로드할 수 있습니다.");
        }
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));

        // 썸네일 업로드 및 저장
        String thumbnailUrl = firebaseImageService.uploadImage(thumbnailImage, "content/" + contentId + "/thumbnail");
        ContentImage thumbnail = new ContentImage(content, thumbnailUrl, (byte) 0); // orderIndex 0
        thumbnail.setThumbnail(true);
        contentImageRepository.save(thumbnail);

        // 일반 이미지 업로드 및 저장 (썸네일 제외)
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String imageUrl = firebaseImageService.uploadImage(image, "content/" + contentId + "/image_" + (i+1));
                ContentImage contentImage = new ContentImage(content, imageUrl, (byte) (i+1)); // orderIndex 1부터
                contentImage.setThumbnail(false);
                contentImageRepository.save(contentImage);
            }
        }
    }

    // 여러 이미지 ID를 받아 일괄 삭제
    public void deleteContentImagesBatch(List<Long> imageIds) {
        for (Long id : imageIds) {
            ContentImage contentImage = contentImageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이미지 ID가 포함되어 있습니다."));
            // 실제 이미지 파일도 삭제
            firebaseImageService.deleteImage(contentImage.getImageUrl());
            contentImageRepository.delete(contentImage);
        }
    }

    // 유지할 이미지 ID 리스트와 새 이미지, 썸네일을 함께 받아 컨텐츠 이미지 전체를 수정
    public void updateContentImages(Long contentId, List<Long> remainingImageIds, List<MultipartFile> newImages, MultipartFile thumbnailImage, Long thumbnailRemainImageId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));

        // 1. 기존 이미지 조회
        List<ContentImage> existingImages = contentImageRepository.findAllById(remainingImageIds);
        List<ContentImage> allImages = contentImageRepository.findAllByContent(content);

        // 새 이미지 개수 검증
        if (newImages != null && newImages.size() > 5) {
            throw new IllegalArgumentException("상세 이미지는 최대 5개까지 업로드할 수 있습니다.");
        }

        // 2. 삭제할 이미지 처리
        for (ContentImage img : allImages) {
            if (!remainingImageIds.contains(img.getContentImageId())) {
                firebaseImageService.deleteImage(img.getImageUrl());
                contentImageRepository.delete(img);
            }
        }

        // 3. 새 이미지 업로드 및 추가
        if (newImages != null) {
            int orderIndex = existingImages.size();
            for (MultipartFile newImage : newImages) {
                String imageUrl = firebaseImageService.uploadImage(newImage, "content/" + contentId + "/image_" + System.currentTimeMillis());
                ContentImage contentImage = new ContentImage(content, imageUrl, (byte) (orderIndex + 1));
                contentImage.setThumbnail(false);
                contentImageRepository.save(contentImage);
                orderIndex++;
            }
        }

        // 4. 썸네일 처리
        // 4-1) 새 썸네일 이미지가 있다면 새로 추가 + 기존 썸네일 false 처리
        if (thumbnailImage != null) {
            for (ContentImage img : contentImageRepository.findAllByContent(content)) {
                img.setThumbnail(false);
                contentImageRepository.save(img);
            }
            String thumbnailUrl = firebaseImageService.uploadImage(thumbnailImage, "content/" + contentId + "/thumbnail_" + System.currentTimeMillis());
            ContentImage newThumbnail = new ContentImage(content, thumbnailUrl, (byte) 0);
            newThumbnail.setThumbnail(true);
            contentImageRepository.save(newThumbnail);
        } else if (thumbnailRemainImageId != null) {
            // 4-2) 기존 이미지 중 명시한 이미지 썸네일 지정
            boolean found = false;
            for (ContentImage img : contentImageRepository.findAllByContent(content)) {
                if (img.getContentImageId().equals(thumbnailRemainImageId)) {
                    img.setThumbnail(true);
                    found = true;
                } else {
                    img.setThumbnail(false);
                }
                contentImageRepository.save(img);
            }
            if (!found) {
                throw new IllegalArgumentException("썸네일로 지정한 기존 이미지가 존재하지 않습니다.");
            }
        } else {
            // 4-3) 기존 썸네일 이미지가 남아있으면 유지, 없으면 첫 번째 이미지 썸네일 지정
            List<ContentImage> imgs = contentImageRepository.findAllByContent(content);
            boolean hasThumbnail = imgs.stream().anyMatch(ContentImage::isThumbnail);
            if (!hasThumbnail && !imgs.isEmpty()) {
                imgs.get(0).setThumbnail(true);
                contentImageRepository.save(imgs.get(0));
            }
        }
    }
}

