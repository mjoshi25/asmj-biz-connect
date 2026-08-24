package com.joshi.twitterclone.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String displayName;
    private String email;
    private String password;
}