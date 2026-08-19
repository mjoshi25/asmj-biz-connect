package com.joshi.twitterclone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EditProfileRequest {

    @NotBlank(message = "Display name cannot be empty")
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    private String displayName;

    @Size(max = 160, message = "Bio cannot exceed 160 characters")
    private String bio;

    private MultipartFile avatar;
    private MultipartFile banner;
}