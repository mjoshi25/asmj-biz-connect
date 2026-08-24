package com.joshi.twitterclone.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class EditProfileRequest {
    private String displayName;
    private String bio;
    private String location;
    private String website;
    private MultipartFile avatar;
    private MultipartFile banner;
}