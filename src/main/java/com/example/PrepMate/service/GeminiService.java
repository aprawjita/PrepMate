package com.example.PrepMate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private String cleanJsonResponse(String rawResponse) {
        if (rawResponse == null) return "";
        return rawResponse.replace("```json", "").replace("```", "").trim();
    }

    public String generateSyllabusJson(String syllabusText) throws Exception {
        String prompt = "You are a strict data parser. Read the following syllabus text and extract the units and topics. " +
                        "Return ONLY a clean JSON array. " +
                        "Format: [{\"unitName\": \"Unit 1\", \"topics\": [\"topic1\", \"topic2\"]}]\n\n" +
                        "Syllabus Text:\n" + syllabusText;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putArray("contents").addObject().putArray("parts").addObject().put("text", prompt);

        root.putObject("generationConfig").put("responseMimeType", "application/json");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode responseNode = mapper.readTree(response.body());

        String rawResponse = responseNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        return cleanJsonResponse(rawResponse);
    }

    public String generateQuiz(String topic) throws Exception {
        String prompt = "You are an expert professor. Generate a 3-question multiple-choice quiz about the following topic: " + topic + ". " +
                        "Return ONLY a clean JSON array. " +
                        "Format: [{\"question\": \"...\", \"options\": [\"A\", \"B\", \"C\", \"D\"], \"answer\": \"The exact correct option string\", \"explanation\": \"...\"}]";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putArray("contents").addObject().putArray("parts").addObject().put("text", prompt);

        root.putObject("generationConfig").put("responseMimeType", "application/json");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode responseNode = mapper.readTree(response.body());

        String rawResponse = responseNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        return cleanJsonResponse(rawResponse);
    }

    public String generateAdvancedExam(List<String> selectedTopics, String pyqText) throws Exception {
        String topicsString = String.join(", ", selectedTopics);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are a strict, expert university professor. Generate a mock exam covering these specific topics: ").append(topicsString).append(". ");

        if (pyqText != null && !pyqText.isEmpty()) {
            promptBuilder.append("Take strong inspiration from the difficulty, tone, and format of these Previous Year Questions (PYQs): \n").append(pyqText).append("\n\n");
        }

        promptBuilder.append("Return ONLY a clean JSON object. Format EXACTLY like this:\n")
                     .append("{\n")
                     .append("  \"mcqs\": [ {\"question\": \"...\", \"options\": [\"A\",\"B\",\"C\",\"D\"], \"answer\": \"exact option\", \"explanation\": \"...\"} ],\n")
                     .append("  \"longQuestion\": {\"question\": \"... (7 marks)\", \"gradingRubric\": [\"Key point 1 to mention\", \"Key point 2\", \"Key point 3\"]}\n")
                     .append("}");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.putArray("contents").addObject().putArray("parts").addObject().put("text", promptBuilder.toString());

        root.putObject("generationConfig").put("responseMimeType", "application/json");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode responseNode = mapper.readTree(response.body());

        String rawResponse = responseNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        return cleanJsonResponse(rawResponse);
    }
}