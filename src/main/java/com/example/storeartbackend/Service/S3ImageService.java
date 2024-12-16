package com.example.storeartbackend.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j // 로그를 사용하기 위한 Lombok 어노테이션
@RequiredArgsConstructor // 생성자를 자동 생성해주는 Lombok 어노테이션
@Component // Spring에서 Bean으로 등록하기 위한 어노테이션
public class S3ImageService {

    private final AmazonS3 amazonS3; // S3 연동을 위한 AmazonS3 객체

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName; // S3 버킷 이름

    // 이미지 업로드 메서드
    public String upload(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new S3Exception("파일이 비어있거나 파일명이 유효하지 않습니다.");
        }
        return this.uploadImage(image);
    }

    // 업로드된 이미지 파일을 S3에 저장하는 메서드
    private String uploadImage(MultipartFile image) {
        this.validateImageFileExtension(image.getOriginalFilename()); // 파일 확장자 검증
        try {
            return this.uploadImageToS3(image); // S3에 파일 업로드
        } catch (IOException e) {
            throw new S3Exception("이미지 업로드 중 IO 예외가 발생했습니다.", e);
        }
    }

    // 파일 확장자 유효성 검사 메서드
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf("."); // 마지막 "." 위치 찾기
        if (lastDotIndex == -1) {
            throw new S3Exception("파일에 확장자가 존재하지 않습니다.");
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase(); // 확장자 추출
        List<String> allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif"); // 허용된 확장자 목록

        if (!allowedExtensionList.contains(extension)) {
            throw new S3Exception("허용되지 않은 파일 확장자입니다: " + extension);
        }
    }

    // S3에 이미지를 업로드하는 메서드
    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); // 원본 파일명
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 추출

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename; // 고유한 파일명 생성

        InputStream is = image.getInputStream(); // 파일의 InputStream 생성
        byte[] bytes = IOUtils.toByteArray(is); // InputStream을 바이트 배열로 변환

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension); // 파일 타입 설정
        metadata.setContentLength(bytes.length); // 파일 크기 설정
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes); // ByteArrayInputStream 생성

        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead); // S3 퍼블릭 읽기 권한 부여
            amazonS3.putObject(putObjectRequest); // S3에 파일 업로드
        } catch (Exception e) {
            throw new S3Exception("S3에 파일 업로드 중 오류가 발생했습니다.", e);
        } finally {
            byteArrayInputStream.close(); // 리소스 정리
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString(); // 업로드된 파일 URL 반환
    }

    // S3에서 이미지를 삭제하는 메서드
    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress); // 이미지 주소에서 키 추출
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key)); // S3에서 파일 삭제
        } catch (Exception e) {
            throw new S3Exception("S3에서 이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }

    // 이미지 주소에서 S3 키 추출
    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress); // URL 객체 생성
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8"); // URL 디코딩
            return decodingKey.substring(1); // 앞에 있는 '/' 제거 후 반환
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new S3Exception("이미지 주소에서 키를 추출하는 중 오류가 발생했습니다.", e);
        }
    }

    // 사용자 정의 S3Exception 클래스
    public static class S3Exception extends RuntimeException {
        public S3Exception(String message) {
            super(message);
        }

        public S3Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

