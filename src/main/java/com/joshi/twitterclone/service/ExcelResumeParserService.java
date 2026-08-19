package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ResumeData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class ExcelResumeParserService {

    public ResumeData parseExcelResume(MultipartFile file) throws Exception {
        ResumeData data = new ResumeData();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            
            // 1. Profile Sheet
            Sheet profileSheet = workbook.getSheet("Profile");
            if (profileSheet != null) {
                for (Row row : profileSheet) {
                    if (row.getRowNum() == 0) continue; // Skip header
                    String field = getCellValue(row.getCell(0)).toLowerCase();
                    String value = getCellValue(row.getCell(1));

                    switch (field) {
                        case "name" -> data.setFullName(value);
                        case "email" -> data.setEmail(value);
                        case "phone" -> data.setPhone(value);
                        case "location" -> data.setLocation(value);
                        case "linkedin" -> data.setLinkedIn(value);
                        case "github" -> data.setGithub(value);
                        case "summary" -> data.setSummary(value);
                    }
                }
            }

            // 2. Experience Sheet
            Sheet expSheet = workbook.getSheet("Experience");
            if (expSheet != null) {
                for (Row row : expSheet) {
                    if (row.getRowNum() == 0) continue;
                    if (isRowEmpty(row)) continue;

                    ResumeData.ExperienceItem item = new ResumeData.ExperienceItem();
                    item.setCompany(getCellValue(row.getCell(0)));
                    item.setRole(getCellValue(row.getCell(1)));
                    item.setDuration(getCellValue(row.getCell(2)));
                    item.setLocation(getCellValue(row.getCell(3)));
                    item.setDescription(getCellValue(row.getCell(4)));
                    data.getExperiences().add(item);
                }
            }

            // 3. Education Sheet
            Sheet eduSheet = workbook.getSheet("Education");
            if (eduSheet != null) {
                for (Row row : eduSheet) {
                    if (row.getRowNum() == 0) continue;
                    if (isRowEmpty(row)) continue;

                    ResumeData.EducationItem item = new ResumeData.EducationItem();
                    item.setInstitution(getCellValue(row.getCell(0)));
                    item.setDegree(getCellValue(row.getCell(1)));
                    item.setYear(getCellValue(row.getCell(2)));
                    item.setGrade(getCellValue(row.getCell(3)));
                    data.getEducation().add(item);
                }
            }

            // 4. Skills Sheet
            Sheet skillSheet = workbook.getSheet("Skills");
            if (skillSheet != null) {
                for (Row row : skillSheet) {
                    if (row.getRowNum() == 0) continue;
                    String skill = getCellValue(row.getCell(0));
                    if (!skill.isBlank()) {
                        data.getSkills().add(skill);
                    }
                }
            }

            // 5. Projects Sheet
            Sheet projSheet = workbook.getSheet("Projects");
            if (projSheet != null) {
                for (Row row : projSheet) {
                    if (row.getRowNum() == 0) continue;
                    if (isRowEmpty(row)) continue;

                    ResumeData.ProjectItem item = new ResumeData.ProjectItem();
                    item.setTitle(getCellValue(row.getCell(0)));
                    item.setTechStack(getCellValue(row.getCell(1)));
                    item.setDescription(getCellValue(row.getCell(2)));
                    item.setLink(getCellValue(row.getCell(3)));
                    data.getProjects().add(item);
                }
            }
        }

        return data;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        Cell cell = row.getCell(0);
        return cell == null || cell.getStringCellValue().trim().isEmpty();
    }
}