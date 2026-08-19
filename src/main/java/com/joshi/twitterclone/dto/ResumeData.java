package com.joshi.twitterclone.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ResumeData {
    // Profile
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String linkedIn;
    private String github;
    private String summary;

    // Passport Photo (Base64 data URI format)
    private String photoBase64;

    // Sections
    private List<ExperienceItem> experiences = new ArrayList<>();
    private List<EducationItem> education = new ArrayList<>();
    private List<String> skills = new ArrayList<>();
    private List<ProjectItem> projects = new ArrayList<>();

    @Data
    public static class ExperienceItem {
        private String company;
        private String role;
        private String duration;
        private String location;
        private String description;
    }

    @Data
    public static class EducationItem {
        private String institution;
        private String degree;
        private String year;
        private String grade;
    }

    @Data
    public static class ProjectItem {
        private String title;
        private String techStack;
        private String description;
        private String link;
    }
}