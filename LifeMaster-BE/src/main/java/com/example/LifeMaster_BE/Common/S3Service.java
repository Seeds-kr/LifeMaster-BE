package com.example.LifeMaster_BE.Common;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Component
@Service
public class S3Service {

    private final AmazonS3 s3Client; // 의존성 주입을 통한 S3 클라이언트 사용

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName; // static 제거

    public String uploadFileImage(MultipartFile image, String file, String contentsType) {
        try {
            String fileName =
                    file + "/" + createFileImageName(image.getOriginalFilename(), contentsType);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());

            s3Client.putObject(bucketName, fileName, image.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3.", e);
        }
    }

    public String updateFileImage(String imageFileUrl, MultipartFile newImage) {
        try {
            String fileName = imageFileUrl.substring(imageFileUrl.lastIndexOf('/') + 1);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(newImage.getContentType());
            metadata.setContentLength(newImage.getSize());

            s3Client.putObject(bucketName, fileName, newImage.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3.", e);
        }
    }

    private String createFileImageName(String fileName, String contentsType) {
        String fileExtension = "";

        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".")); // 확장자 분리
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter) + contentsType + fileExtension;
    }

    public void deleteFileImage(String imageFileUrl) {
        String fileName = imageFileUrl.substring(imageFileUrl.lastIndexOf('/') + 1);
        try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3.", e);
        }
    }
}

