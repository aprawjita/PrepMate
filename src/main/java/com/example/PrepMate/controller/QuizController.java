package com.example.PrepMate.controller;

import com.example.PrepMate.model.CourseUnit;
import com.example.PrepMate.repository.CourseUnitRepository;
import com.example.PrepMate.service.GeminiService;
import com.example.PrepMate.service.SyllabusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final CourseUnitRepository courseUnitRepository;
    private final GeminiService geminiService;
    private final SyllabusService syllabusService;

    public QuizController(CourseUnitRepository courseUnitRepository, 
                          GeminiService geminiService, 
                          SyllabusService syllabusService) {
        this.courseUnitRepository = courseUnitRepository;
        this.geminiService = geminiService;
        this.syllabusService = syllabusService;
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generateRandomQuiz() {
        try {
            List<CourseUnit> units = courseUnitRepository.findAll();
            if (units.isEmpty()) {
                return ResponseEntity.badRequest().body("No syllabus data found in the database. Upload a PDF first!");
            }

            Random random = new Random();
            CourseUnit randomUnit = units.get(random.nextInt(units.size()));
            List<String> topics = randomUnit.getTopics();
            String randomTopic = topics.get(random.nextInt(topics.size()));

            System.out.println("\nDEBUG: AI is writing a quiz for the topic: [" + randomTopic + "]...");

            String quizJson = geminiService.generateQuiz(randomTopic);
            return ResponseEntity.ok(quizJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error generating quiz: " + e.getMessage());
        }
    }

    @PostMapping("/exam")
    public ResponseEntity<String> generateExam(@RequestParam(value = "pyqFile", required = false) MultipartFile pyqFile) {
        try {
            List<CourseUnit> units = courseUnitRepository.findAll();
            if (units.isEmpty()) {
                return ResponseEntity.badRequest().body("No syllabus data found!");
            }

            List<String> allTopics = new ArrayList<>();
            for (CourseUnit unit : units) {
                allTopics.addAll(unit.getTopics());
            }
            
            Collections.shuffle(allTopics);
            List<String> selectedTopics = allTopics.subList(0, Math.min(5, allTopics.size()));

            String pyqText = "";
            if (pyqFile != null && !pyqFile.isEmpty()) {
                System.out.println("DEBUG: Reading PYQ File for inspiration...");
                pyqText = syllabusService.extractTextFromPdf(pyqFile);
            }

            System.out.println("DEBUG: Generating Exam for topics: " + selectedTopics);

            String examJson = geminiService.generateAdvancedExam(selectedTopics, pyqText);

            return ResponseEntity.ok(examJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}