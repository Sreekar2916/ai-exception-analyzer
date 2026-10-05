package com.sreekar.aiexceptionanalyzer.controller;

import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeRequest;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.exception.AIClientException;
import com.sreekar.aiexceptionanalyzer.service.ExceptionAnalyzerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExceptionAnalyzerController.class)
class ExceptionAnalyzerControllerTest {

    private static final String URL = "/api/v1/exceptions/analyze";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExceptionAnalyzerService exceptionAnalyzerService;

    @Test
    void returnsAnalysisForValidRequest() throws Exception {
        when(exceptionAnalyzerService.analyze(any(ExceptionAnalyzeRequest.class))).thenReturn(
                new ExceptionAnalyzeResponse(
                        "NullPointerException", "HIGH", "The User object is null when getName() is called.",
                        List.of("The user does not exist in the database"),
                        "Check whether the User object exists before calling getName().",
                        List.of("Validate the repository response")));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "exceptionType": "NullPointerException",
                                  "message": "Cannot invoke User.getName() because user is null",
                                  "stackTrace": "java.lang.NullPointerException at UserService.java:45"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exceptionType").value("NullPointerException"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.possibleCauses[0]").value("The user does not exist in the database"))
                .andExpect(jsonPath("$.prevention[0]").value("Validate the repository response"));
    }

    @Test
    void returnsBadRequestWhenExceptionTypeIsBlank() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exceptionType": "", "message": "Something failed"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.exceptionType").value("exceptionType must not be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(exceptionAnalyzerService);
    }

    @Test
    void returnsBadRequestWhenMessageIsBlank() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exceptionType": "NullPointerException", "message": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").value("message must not be blank"));

        verifyNoInteractions(exceptionAnalyzerService);
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsBadGatewayWhenAIServiceFails() throws Exception {
        when(exceptionAnalyzerService.analyze(any(ExceptionAnalyzeRequest.class)))
                .thenThrow(new AIClientException("Groq API returned HTTP 500"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exceptionType": "NullPointerException", "message": "user is null"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Unable to analyze the exception using the AI service"));
    }

    @Test
    void returnsInternalServerErrorForUnexpectedFailures() throws Exception {
        when(exceptionAnalyzerService.analyze(any(ExceptionAnalyzeRequest.class)))
                .thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exceptionType": "NullPointerException", "message": "user is null"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
