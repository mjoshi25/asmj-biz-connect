package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.ResumeData;
import com.joshi.twitterclone.service.ExcelResumeParserService;
import com.joshi.twitterclone.service.ResumePdfService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Controller
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ExcelResumeParserService parserService;
    private final ResumePdfService pdfService;

    @GetMapping
    public String viewResumeCreator(Model model) {
        return "resume-builder";
    }

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "photo", required = false) MultipartFile photo,
                              HttpSession session,
                              Model model) {
        try {
            ResumeData resumeData = parserService.parseExcelResume(file);

            // Handle optional Passport Photo upload
            if (photo != null && !photo.isEmpty()) {
                String base64Image = "data:" + photo.getContentType() + ";base64," + 
                        Base64.getEncoder().encodeToString(photo.getBytes());
                resumeData.setPhotoBase64(base64Image);
            }

            session.setAttribute("CURRENT_RESUME_DATA", resumeData);
            model.addAttribute("resume", resumeData);
            return "fragments/resume-preview :: resume-card";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error parsing Excel or Photo: " + e.getMessage());
            return "fragments/resume-preview :: error-alert";
        }
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam(value = "template", defaultValue = "modern-template") String template,
                                              HttpSession session) throws Exception {
        ResumeData data = (ResumeData) session.getAttribute("CURRENT_RESUME_DATA");
        if (data == null) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = pdfService.generateResumePdf(data, template);

        String safeName = (data.getFullName() != null && !data.getFullName().isBlank()) 
                ? data.getFullName().replaceAll("[^a-zA-Z0-9]", "_") 
                : "Resume";
        String filename = safeName + "_" + template.replace("-template", "") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/template/download")
    public ResponseEntity<byte[]> downloadSampleTemplate() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 1. Profile Sheet
            Sheet profile = workbook.createSheet("Profile");
            createRow(profile, 0, "Field", "Value");
            createRow(profile, 1, "Name", "Alex Rivera");
            createRow(profile, 2, "Email", "alex.rivera@example.com");
            createRow(profile, 3, "Phone", "+1 (555) 234-5678");
            createRow(profile, 4, "Location", "San Francisco, CA");
            createRow(profile, 5, "LinkedIn", "linkedin.com/in/alexrivera");
            createRow(profile, 6, "Github", "github.com/alexrivera");
            createRow(profile, 7, "Summary", "Full-Stack Software Engineer with 5+ years of experience designing high-throughput distributed systems and modern reactive web applications.");

            // 2. Experience Sheet
            Sheet exp = workbook.createSheet("Experience");
            createRow(exp, 0, "Company", "Role", "Duration", "Location", "Description");
            createRow(exp, 1, "Nexus Tech", "Senior Backend Engineer", "2022 - Present", "San Francisco, CA", "Architected low-latency STOMP WebSocket messaging clusters and reactive feeds serving 200k active users.");
            createRow(exp, 2, "Starlight Systems", "Software Engineer", "2020 - 2022", "Austin, TX", "Built microservices using Spring Boot, Kafka, and MongoDB with 99.99% uptime.");

            // 3. Education Sheet
            Sheet edu = workbook.createSheet("Education");
            createRow(edu, 0, "Institution", "Degree", "Year", "Grade");
            createRow(edu, 1, "University of California, Berkeley", "B.S. in Computer Science", "2020", "3.9 GPA");

            // 4. Skills Sheet
            Sheet skills = workbook.createSheet("Skills");
            createRow(skills, 0, "Skill");
            createRow(skills, 1, "Java / Spring Boot");
            createRow(skills, 2, "MongoDB / PostgreSQL");
            createRow(skills, 3, "WebSockets / STOMP");
            createRow(skills, 4, "Docker & Kubernetes");
            createRow(skills, 5, "Thymeleaf & TailwindCSS");

            // 5. Projects Sheet
            Sheet proj = workbook.createSheet("Projects");
            createRow(proj, 0, "Title", "TechStack", "Description", "Link");
            createRow(proj, 1, "Distributed Ad Exchange", "Spring Boot, Redis, MongoDB", "Real-time bidding pipeline with sub-50ms latency.", "https://github.com/alex/ad-engine");

            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Nexus_Resume_Template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    private void createRow(Sheet sheet, int rowNum, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}