package com.sreekar.aiexceptionanalyzer.client;

import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;

public interface AIClient {

    ExceptionAnalyzeResponse analyze(String prompt);
}
