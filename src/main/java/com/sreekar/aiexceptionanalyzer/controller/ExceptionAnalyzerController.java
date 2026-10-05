package com.sreekar.aiexceptionanalyzer.controller;

import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeRequest;
import com.sreekar.aiexceptionanalyzer.dto.ExceptionAnalyzeResponse;
import com.sreekar.aiexceptionanalyzer.service.ExceptionAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exceptions")
public class ExceptionAnalyzerController {

    private final ExceptionAnalyzerService exceptionAnalyzerService;

    public ExceptionAnalyzerController(ExceptionAnalyzerService exceptionAnalyzerService) {
        this.exceptionAnalyzerService = exceptionAnalyzerService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExceptionAnalyzeResponse analyze(@Valid @RequestBody ExceptionAnalyzeRequest request) {
        return exceptionAnalyzerService.analyze(request);
    }
}
