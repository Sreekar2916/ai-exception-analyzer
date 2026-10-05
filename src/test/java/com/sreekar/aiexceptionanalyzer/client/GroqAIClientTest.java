package com.sreekar.aiexceptionanalyzer.client;

import com.sreekar.aiexceptionanalyzer.config.AIConfig;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.exception.AIClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GroqAIClientTest {

    private static final String BASE_URL = "https://groq.test/openai/v1";
    private static final String ENDPOINT = BASE_URL + "/chat/completions";
    private static final String ANALYSIS_JSON = """
            {
              "exceptionType": "NullPointerException",
              "severity": "high",
              "rootCause": "The User object is null.",
              "possibleCauses": ["User not found"],
              "suggestedFix": "Check for null before calling getName().",
              "prevention": ["Add unit tests for null cases"]
            }
            """;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private MockRestServiceServer server;
    private GroqAIClient client;

    @BeforeEach
    void setUp() {
        client = createClient("test-key");
    }

    private GroqAIClient createClient(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        AIConfig config = new AIConfig(apiKey, BASE_URL, "test-model", Duration.ofSeconds(5));
        return new GroqAIClient(config, objectMapper, builder);
    }

    private String groqResponse(String text) {
        return objectMapper.writeValueAsString(Map.of(
                "choices", new Object[]{Map.of("message", Map.of("role", "assistant", "content", text))}));
    }

    private void respondWith(String text) {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(groqResponse(text), MediaType.APPLICATION_JSON));
    }

    @Test
    void sendsPromptAndParsesAnalysis() {
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("my prompt"))
                .andExpect(jsonPath("$.response_format.type").value("json_schema"))
                .andExpect(jsonPath("$.response_format.json_schema.strict").value(true))
                .andExpect(jsonPath("$.response_format.json_schema.schema.required.length()").value(6))
                .andRespond(withSuccess(groqResponse(ANALYSIS_JSON), MediaType.APPLICATION_JSON));

        ExceptionAnalyzeResponse response = client.analyze("my prompt");

        server.verify();
        assertThat(response.exceptionType()).isEqualTo("NullPointerException");
        assertThat(response.severity()).isEqualTo("HIGH");
        assertThat(response.possibleCauses()).containsExactly("User not found");
        assertThat(response.prevention()).containsExactly("Add unit tests for null cases");
    }

    @Test
    void stripsMarkdownCodeFences() {
        respondWith("\n  ```json\n" + ANALYSIS_JSON + "```\n  ");

        assertThat(client.analyze("prompt").rootCause()).isEqualTo("The User object is null.");
    }

    @Test
    void failsWhenAnalysisIsNotValidJson() {
        respondWith("Sorry, I cannot help with that.");

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("not valid JSON");
    }

    @Test
    void failsWhenFieldsAreMissing() {
        respondWith("""
                {"exceptionType": "NullPointerException", "severity": "HIGH"}
                """);

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("missing fields");
    }

    @Test
    void failsWhenSeverityIsUnknown() {
        respondWith(ANALYSIS_JSON.replace("\"high\"", "\"EXTREME\""));

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("unknown severity");
    }

    @Test
    void failsWhenResponseHasNoChoices() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{\"choices\": []}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("did not contain any text");
    }

    @Test
    void failsWhenResponseBodyIsEmpty() {
        server.expect(requestTo(ENDPOINT)).andRespond(withSuccess());

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void wrapsHttpErrors() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("{\"error\": \"quota\"}"));

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessage("Groq API returned HTTP 429");
    }

    @Test
    void wrapsNetworkErrors() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessage("Failed to call Groq API")
                .hasRootCauseInstanceOf(IOException.class);
    }

    @Test
    void failsFastWhenApiKeyIsMissing() {
        GroqAIClient clientWithoutKey = createClient("");

        assertThatThrownBy(() -> clientWithoutKey.analyze("prompt"))
                .isInstanceOf(AIClientException.class)
                .hasMessageContaining("GROQ_API_KEY");
    }
}
