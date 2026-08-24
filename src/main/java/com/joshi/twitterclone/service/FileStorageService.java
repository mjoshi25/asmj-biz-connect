package com.joshi.twitterclone.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a raw file or image to Cloudinary.
     */
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String contentType = file.getContentType();
            String resourceType = "auto";

            if (contentType != null && !contentType.startsWith("image/") && !contentType.startsWith("video/")) {
                resourceType = "raw";
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "asmj_biz_connect"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Uploaded file to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Uploads an image with automatic format and quality optimization.
     */
    public String saveImageOptimized(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", "asmj_biz_connect/images",
                            "fetch_format", "auto",
                            "quality", "auto"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Uploaded optimized image to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Cloudinary image upload failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Generates a transformed thumbnail URL directly via Cloudinary CDN.
     * Example: 400x400 auto-cropped thumbnail with smart facial focus.
     */
    public String getThumbnailUrl(String imageUrl, int width, int height) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return imageUrl;
        }

        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            return imageUrl;
        }

        return cloudinary.url()
                .transformation(new Transformation<>()
                        .width(width)
                        .height(height)
                        .crop("fill")
                        .gravity("auto")
                        .quality("auto")
                        .fetchFormat("auto"))
                .generate(publicId);
    }

    /**
     * Deletes an asset from Cloudinary using its secure URL.
     */
    public void deleteByUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("cloudinary.com")) {
            return;
        }

        String publicId = extractPublicId(fileUrl);
        if (publicId == null) {
            return;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted asset from Cloudinary [{}]: {}", publicId, result.get("result"));
        } catch (IOException e) {
            log.error("Failed to delete asset [{}] from Cloudinary: {}", publicId, e.getMessage());
        }
    }

    /**
     * Extracts the public_id from a Cloudinary URL.
     * Example URL: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/asmj_biz_connect/images/sample.jpg
     * Returns: asmj_biz_connect/images/sample
     */
    public String extractPublicId(String imageUrl) {
        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) return null;

            String pathAfterUpload = imageUrl.substring(uploadIndex + "/upload/".length());

            // Remove version tag (e.g. v1712345678/) if present
            if (pathAfterUpload.matches("^v\\d+/.*")) {
                pathAfterUpload = pathAfterUpload.substring(pathAfterUpload.indexOf("/") + 1);
            }

            // Remove file extension (.jpg, .png, .webp, etc.)
            int dotIndex = pathAfterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                return pathAfterUpload.substring(0, dotIndex);
            }

            return pathAfterUpload;
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", imageUrl);
            return null;
        }
    }
}