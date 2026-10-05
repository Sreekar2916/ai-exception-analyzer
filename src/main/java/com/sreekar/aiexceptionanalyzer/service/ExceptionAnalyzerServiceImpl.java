package com.sreekar.aiexceptionanalyzer.service;

import com.sreekar.aiexceptionanalyzer.client.AIClient;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeRequest;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.util.PromptBuilder;
import org.springframework.stereotype.Service;

@Service
public class ExceptionAnalyzerServiceImpl implements ExceptionAnalyzerService {

    private final PromptBuilder promptBuilder;
    private final AIClient aiClient;

    public ExceptionAnalyzerServiceImpl(PromptBuilder promptBuilder, AIClient aiClient) {
        this.promptBuilder = promptBuilder;
        this.aiClient = aiClient;
    }

    @Override
    public ExceptionAnalyzeResponse analyze(ExceptionAnalyzeRequest request) {
        String prompt = promptBuilder.build(request);
        return aiClient.analyze(prompt);
    }
}
