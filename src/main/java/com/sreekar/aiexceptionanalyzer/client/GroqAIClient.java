package com.sreekar.aiexceptionanalyzer.client;

import com.sreekar.aiexceptionanalyzer.config.AIConfig;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.exception.AIClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GroqAIClient implements AIClient {

    private static final Set<String> SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private static final Map<String, Object> STRING_ARRAY = Map.of(
            "type", "array",
            "items", Map.of("type", "string"));

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "exceptionType", Map.of("type", "string"),
                    "severity", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")),
                    "rootCause", Map.of("type", "string"),
                    "possibleCauses", STRING_ARRAY,
                    "suggestedFix", Map.of("type", "string"),
                    "prevention", STRING_ARRAY),
            "required", List.of("exceptionType", "severity", "rootCause", "possibleCauses", "suggestedFix", "prevention"),
            "additionalProperties", false);

    private final AIConfig config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public GroqAIClient(AIConfig config, ObjectMapper objectMapper) {
        this(config, objectMapper, RestClient.builder().requestFactory(requestFactory(config)));
    }

    GroqAIClient(AIConfig config, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.baseUrl(config.baseUrl()).build();
    }

    @Override
    public ExceptionAnalyzeResponse analyze(String prompt) {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new AIClientException("Groq API key is not configured. Set the GROQ_API_KEY environment variable.");
        }
        String responseBody = callGroq(prompt);
        String text = extractText(responseBody);
        return toAnalyzeResponse(stripCodeFences(text));
    }

    private String callGroq(String prompt) {
        try {
            return restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + config.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(buildRequestBody(prompt)))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            throw new AIClientException("Groq API returned HTTP " + ex.getStatusCode().value(), ex);
        } catch (RestClientException ex) {
            throw new AIClientException("Failed to call Groq API", ex);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", config.model(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2,
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", Map.of(
                                "name", "exception_analysis",
                                "strict", true,
                                "schema", RESPONSE_SCHEMA)));
    }

    private String extractText(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AIClientException("Groq API returned an empty response");
        }
        try {
            JsonNode textNode = objectMapper.readTree(responseBody)
                    .path("choices").path(0)
                    .path("message").path("content");
            if (!textNode.isString() || textNode.asString().isBlank()) {
                throw new AIClientException("Groq API response did not contain any text");
            }
            return textNode.asString();
        } catch (JacksonException ex) {
            throw new AIClientException("Groq API returned a response that is not valid JSON", ex);
        }
    }

    private String stripCodeFences(String text) {
        String result = text.strip();
        if (result.startsWith("```")) {
            result = result.substring(result.indexOf('\n') + 1);
            if (result.endsWith("```")) {
                result = result.substring(0, result.length() - 3);
            }
        }
        return result.strip();
    }

    private ExceptionAnalyzeResponse toAnalyzeResponse(String json) {
        ExceptionAnalyzeResponse response;
        try {
            response = objectMapper.readValue(json, ExceptionAnalyzeResponse.class);
        } catch (JacksonException ex) {
            throw new AIClientException("Groq returned an analysis that is not valid JSON", ex);
        }
        validate(response);
        return new ExceptionAnalyzeResponse(
                response.exceptionType().strip(),
                response.severity().strip().toUpperCase(),
                response.rootCause().strip(),
                response.possibleCauses(),
                response.suggestedFix().strip(),
                response.prevention());
    }

    private void validate(ExceptionAnalyzeResponse response) {
        if (response == null
                || isBlank(response.exceptionType())
                || isBlank(response.severity())
                || isBlank(response.rootCause())
                || isBlank(response.suggestedFix())
                || response.possibleCauses() == null
                || response.prevention() == null) {
            throw new AIClientException("Groq returned an analysis with missing fields");
        }
        if (!SEVERITIES.contains(response.severity().strip().toUpperCase())) {
            throw new AIClientException("Groq returned an unknown severity: " + response.severity());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static SimpleClientHttpRequestFactory requestFactory(AIConfig config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.timeout());
        factory.setReadTimeout(config.timeout());
        return factory;
    }
}
