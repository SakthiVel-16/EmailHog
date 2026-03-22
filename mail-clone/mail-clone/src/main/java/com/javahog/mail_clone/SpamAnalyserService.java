package com.javahog.mail_clone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SpamAnalyserService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpamAnalysisResult analyseEmail(Email email) {
        try {
            String prompt = buildPrompt(email);

            // Build Gemini API request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json"));

            // Call Gemini API with correct headers and retry
            // Call Gemini API — key passed as URL parameter
            String rawResponse = null;
            int maxRetries = 3;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    String urlWithKey = apiUrl + "?key=" + apiKey;
                    rawResponse = webClient.post()
                            .uri(urlWithKey)
                            .header("Content-Type", "application/json")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    break;
                } catch (Exception retryEx) {
                    if (attempt == maxRetries)
                        throw retryEx;
                    long waitSeconds = (long) Math.pow(2, attempt) * 5;
                    System.out.println("Gemini rate limit — waiting "
                            + waitSeconds + " seconds before retry " + attempt + "...");
                    Thread.sleep(waitSeconds * 1000L);
                }
            }

            return parseResponse(rawResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return errorResult("AI analysis failed: " + e.getMessage());
        }
    }

    private String buildPrompt(Email email) {
        // Truncate body to avoid exceeding token limits
        String body = email.getBody() != null ? email.getBody() : "";
        if (body.length() > 3000) {
            body = body.substring(0, 3000) + "...(truncated)";
        }

        // Strip HTML tags for cleaner analysis
        String cleanBody = body.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return """
                You are an expert email deliverability and spam analysis engine.
                Analyse the following email and return ONLY a valid JSON response.
                Do NOT include any explanation text, markdown, or code blocks outside the JSON.

                Email details:
                Subject: %s
                From: %s
                To: %s
                Body: %s

                Return this EXACT JSON format:
                {
                  "spamScore": <integer between 0 and 100>,
                  "scoreLabel": "<one of: Excellent, Good, Warning, High Risk, Critical>",
                  "summary": "<one sentence overall assessment>",
                  "issues": [
                    {
                      "severity": "<HIGH or MEDIUM or LOW>",
                      "issue": "<what the specific problem is>",
                      "fix": "<exactly how to fix it>"
                    }
                  ]
                }

                Scoring guide:
                0-20 = Excellent (very likely to reach inbox)
                21-40 = Good (minor issues, low spam risk)
                41-60 = Warning (multiple issues, some filters may flag)
                61-80 = High Risk (significant spam signals)
                81-100 = Critical (will almost certainly land in spam)

                Check for these spam signals:
                - Subject line trigger words (Free, Win, Urgent, Act Now, Limited Time)
                - ALL CAPS usage in subject or body
                - Excessive exclamation marks or question marks
                - Urgency or fear based language
                - Missing or suspicious sender address
                - Image heavy with very little text
                - No plain text alternative
                - Misleading or suspicious links
                - Overly promotional language

                If the email is clean with no issues, return an empty issues array.
                IMPORTANT: Return only the JSON object, nothing else.
                """.formatted(
                email.getSubject(),
                email.getFrom(),
                email.getTo(),
                cleanBody);
    }

    private SpamAnalysisResult parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            // Extract text content from Gemini response structure
            String content = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // Clean up response — remove markdown code blocks if present
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            // Parse the JSON that Gemini returned
            JsonNode resultNode = objectMapper.readTree(content);

            int spamScore = resultNode.path("spamScore").asInt(0);
            String scoreLabel = resultNode.path("scoreLabel").asText("Unknown");
            String summary = resultNode.path("summary").asText("Analysis complete.");

            List<SpamAnalysisResult.Issue> issues = new ArrayList<>();
            JsonNode issuesNode = resultNode.path("issues");
            if (issuesNode.isArray()) {
                for (JsonNode issueNode : issuesNode) {
                    String severity = issueNode.path("severity").asText("LOW");
                    String issue = issueNode.path("issue").asText("");
                    String fix = issueNode.path("fix").asText("");
                    if (!issue.isBlank()) {
                        issues.add(new SpamAnalysisResult.Issue(severity, issue, fix));
                    }
                }
            }

            return new SpamAnalysisResult(spamScore, scoreLabel, summary, issues);

        } catch (Exception e) {
            e.printStackTrace();
            return errorResult("Failed to parse AI response: " + e.getMessage());
        }
    }

    private SpamAnalysisResult errorResult(String message) {
        List<SpamAnalysisResult.Issue> issues = new ArrayList<>();
        issues.add(new SpamAnalysisResult.Issue(
                "HIGH",
                "AI analysis could not be completed",
                message));
        return new SpamAnalysisResult(0, "Unknown", message, issues);
    }
}