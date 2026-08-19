package com.joshi.twitterclone.service;

import com.joshi.twitterclone.dto.ResumeData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResumePdfService {

    private final TemplateEngine templateEngine;

    private static final Set<String> ALLOWED_TEMPLATES = Set.of(
            "modern-template",
            "classic-template",
            "minimal-template",
            "compact-template"
    );

    public byte[] generateResumePdf(ResumeData data, String templateName) throws Exception {
        Context context = new Context();
        context.setVariable("resume", data);

        // Sanitize and validate template name to prevent template path traversal
        String safeTemplate = (templateName != null && ALLOWED_TEMPLATES.contains(templateName.trim()))
                ? templateName.trim()
                : "modern-template";

        String templatePath = "resumes/" + safeTemplate;
        String processedHtml = templateEngine.process(templatePath, context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }
}