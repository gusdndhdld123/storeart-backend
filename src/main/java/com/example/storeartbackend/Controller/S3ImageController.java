package com.example.storeartbackend.Controller;

import com.example.storeartbackend.Service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/images")
public class S3ImageController {

    private final S3ImageService s3ImageService;

    /**
     * 이미지 업로드 엔드포인트
     * @param file 업로드할 MultipartFile 이미지
     * @return 업로드된 이미지의 S3 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3ImageService.upload(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.badRequest().body("이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    /**
     * 이미지 삭제 엔드포인트
     * @param imageUrl 삭제할 이미지의 S3 URL
     * @return 삭제 상태 메시지
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            s3ImageService.deleteImageFromS3(imageUrl);
            return ResponseEntity.ok("이미지가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("이미지 삭제 실패", e);
            return ResponseEntity.badRequest().body("이미지 삭제 중 오류가 발생했습니다.");
        }
    }
}