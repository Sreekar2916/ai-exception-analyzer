package com.sreekar.aiexceptionanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExceptionAnalyzeRequest(

        @NotBlank(message = "exceptionType must not be blank")
        @Size(max = 200, message = "exceptionType must be at most 200 characters")
        String exceptionType,

        @NotBlank(message = "message must not be blank")
        @Size(max = 2000, message = "message must be at most 2000 characters")
        String message,

        @Size(max = 20000, message = "stackTrace must be at most 20000 characters")
        String stackTrace
) {
}
