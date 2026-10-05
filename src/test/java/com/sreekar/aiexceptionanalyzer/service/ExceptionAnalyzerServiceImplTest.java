package com.sreekar.aiexceptionanalyzer.service;

import com.sreekar.aiexceptionanalyzer.client.AIClient;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeRequest;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.exception.AIClientException;
import com.sreekar.aiexceptionanalyzer.util.PromptBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExceptionAnalyzerServiceImplTest {

    private final AIClient aiClient = mock(AIClient.class);
    private final ExceptionAnalyzerService service = new ExceptionAnalyzerServiceImpl(new PromptBuilder(), aiClient);

    private final ExceptionAnalyzeRequest request = new ExceptionAnalyzeRequest(
            "NullPointerException", "Cannot invoke User.getName() because user is null", null);

    @Test
    void sendsBuiltPromptToAIClientAndReturnsAnalysis() {
        ExceptionAnalyzeResponse expected = new ExceptionAnalyzeResponse(
                "NullPointerException", "HIGH", "User is null",
                List.of("User not found"), "Check for null", List.of("Validate inputs"));
        when(aiClient.analyze(anyString())).thenReturn(expected);

        ExceptionAnalyzeResponse actual = service.analyze(request);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(aiClient).analyze(prompt.capture());
        assertThat(actual).isEqualTo(expected);
        assertThat(prompt.getValue())
                .contains("Exception Type: NullPointerException")
                .contains("Cannot invoke User.getName() because user is null");
    }

    @Test
    void propagatesAIClientFailures() {
        when(aiClient.analyze(anyString())).thenThrow(new AIClientException("Groq down"));

        assertThatThrownBy(() -> service.analyze(request)).isInstanceOf(AIClientException.class);
    }
}
