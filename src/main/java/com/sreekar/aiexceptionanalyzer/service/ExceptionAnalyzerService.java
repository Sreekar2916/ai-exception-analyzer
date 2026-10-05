package com.sreekar.aiexceptionanalyzer.service;

import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeRequest;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;

public interface ExceptionAnalyzerService {

    ExceptionAnalyzeResponse analyze(ExceptionAnalyzeRequest request);
}
