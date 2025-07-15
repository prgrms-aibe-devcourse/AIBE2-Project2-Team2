package org.example.backend.firebase;

import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.exception.customException.ImageDeleteException;
import org.example.backend.exception.customException.ImageUploadException;
import org.example.backend.exception.customException.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseImageService {

    // 이미지 업로드 메소드
    private String uploadImage(MultipartFile file, String itemName) {
        try (InputStream inputStream = file.getInputStream()) {
            // 원본 파일명과 확장자 분리
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new InvalidFileException("파일 이름이 비어 있습니다.");
            }

            String ext = "";
            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex != -1) {
                ext = originalFileName.substring(dotIndex); // ".jpg" 등
            }

            // UUID 생성 및 파일명 조합
            String uuid = UUID.randomUUID().toString();
            String newFileName = itemName + "-" + uuid + ext;

            // Firebase Storage 경로 설정
            String blobString = "items/" + newFileName;

            log.info("StorageClient 초기화 확인: {}", StorageClient.getInstance().bucket().getName());

            // 파일 업로드
            StorageClient.getInstance()
                    .bucket()
                    .create(blobString, inputStream, file.getContentType());

            // 공개 URL 생성
            String bucketName = StorageClient.getInstance().bucket().getName();
            String encodedPath = URLEncoder.encode(blobString, StandardCharsets.UTF_8);
            String publicUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucketName + "/o/" + encodedPath + "?alt=media";

            log.info("이미지 업로드 완료: {}", publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            throw new ImageUploadException("이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    // 기존 이미지 삭제 메소드
    private void deleteImage(String imageUrl) {
        try {
            // 이미지 URL에서 Firebase Storage 내 경로(blobString)를 추출
            String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
            String prefix = "/o/";
            int startIndex = decodedUrl.indexOf(prefix) + prefix.length();
            int endIndex = decodedUrl.indexOf("?alt=media");
            String blobString = decodedUrl.substring(startIndex, endIndex);

            // 실제 Firebase Storage에서 파일 삭제
            boolean deleted = StorageClient.getInstance()
                    .bucket()
                    .get(blobString)
                    .delete();

            if (deleted) {
                log.info("기존 이미지 삭제 완료: {}", blobString);
            } else {
                log.warn("기존 이미지 삭제 실패: {}", blobString);
            }
        } catch (Exception e) {
            log.error("기존 이미지 삭제 중 오류 발생", e);
            throw new ImageDeleteException("이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}
