package com.example.PrepMate.controller;

import com.example.PrepMate.model.CourseUnit;
import com.example.PrepMate.repository.CourseUnitRepository;
import com.example.PrepMate.service.GeminiService;
import com.example.PrepMate.service.SyllabusService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;
    private final GeminiService geminiService;
    private final CourseUnitRepository courseUnitRepository;

    // We removed ObjectMapper from here so Spring stops panicking!
    public SyllabusController(SyllabusService syllabusService, 
                              GeminiService geminiService, 
                              CourseUnitRepository courseUnitRepository) {
        this.syllabusService = syllabusService;
        this.geminiService = geminiService;
        this.courseUnitRepository = courseUnitRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSyllabus(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty!");
        }

        try {
            // 1. Read PDF
            String rawText = syllabusService.extractTextFromPdf(file);
            System.out.println("DEBUG: PDF reading finished. Sending to AI...");
            
            // 2. Send to AI
            String aiJsonResult = geminiService.generateSyllabusJson(rawText);
            System.out.println("DEBUG: AI Processing complete! Converting to Java objects...");

            // 3. We build our own translator right here!
            ObjectMapper objectMapper = new ObjectMapper();
            List<CourseUnit> extractedUnits = objectMapper.readValue(
                    aiJsonResult, 
                    new TypeReference<List<CourseUnit>>() {}
            );

            // 4. Save to PostgreSQL Database!
            courseUnitRepository.saveAll(extractedUnits);
            System.out.println("DEBUG: Successfully saved " + extractedUnits.size() + " units to the database!");

            // 5. Return success message
            return ResponseEntity.ok("Syllabus processed and saved to database successfully!\n\n" + aiJsonResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error: " + e.getMessage());
        }
    }
}