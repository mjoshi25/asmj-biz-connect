package com.joshi.twitterclone.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateGroupRequest {
    private String name;
    private List<String> memberUsernames = new ArrayList<>();
}