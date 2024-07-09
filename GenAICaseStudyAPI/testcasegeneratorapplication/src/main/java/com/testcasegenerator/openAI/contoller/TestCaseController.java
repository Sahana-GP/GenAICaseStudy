package com.testcasegenerator.openAI.contoller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TestCaseController {

    private static final Logger LOGGER = Logger.getLogger(TestCaseController.class.getName());

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";

    @PostMapping("/generateTestCases")
    public ResponseEntity<Map<String, String>> generateTestCases(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");
        if (code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>(Map.of("error", "Code cannot be empty or whitespace."), HttpStatus.BAD_REQUEST);
        }

        try {
            String promptText = "Generate unit tests for the following code: " + code;

            // Create the request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "gpt-3.5-turbo");
            payload.put("messages", List.of(Map.of("role", "user", "content", promptText)));
            payload.put("max_tokens", 1000);
            payload.put("temperature", 0.5);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBodyJson = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(OPENAI_API_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            LOGGER.info("Response status code: " + response.statusCode());
            LOGGER.info("Response body: " + response.body());

            // Handle rate limit exceeded (429) error
            if (response.statusCode() == 429) {
                LOGGER.warning("Rate limit reached. Waiting before retrying.");
                Thread.sleep(20000); // Wait for 20 seconds before retrying
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                LOGGER.info("Retry response status code: " + response.statusCode());
                LOGGER.info("Retry response body: " + response.body());
            }

            if (response.statusCode() == 200) {
                JsonNode responseBody = objectMapper.readTree(response.body());
                JsonNode choices = responseBody.get("choices");

                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        String answer = message.get("content").asText();
                        return new ResponseEntity<>(Map.of("testCases", answer), HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>(Map.of("error", "No completions found"), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(Map.of("error", "Failed to get response from OpenAI API"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            LOGGER.severe("Error: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
