package com.sreekar.aiexceptionanalyzer.dto;

import java.util.List;

public record ExceptionAnalyzeResponse(
        String exceptionType,
        String severity,
        String rootCause,
        List<String> possibleCauses,
        String suggestedFix,
        List<String> prevention
) {
}
