package com.sreekar.aiexceptionanalyzer.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionAnalyzeRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequestHasNoViolations() {
        ExceptionAnalyzeRequest request = new ExceptionAnalyzeRequest(
                "NullPointerException", "Cannot invoke User.getName() because user is null", null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void blankExceptionTypeIsInvalid() {
        ExceptionAnalyzeRequest request = new ExceptionAnalyzeRequest(" ", "Some message", null);

        Set<ConstraintViolation<ExceptionAnalyzeRequest>> violations = validator.validate(request);

        assertThat(violations).singleElement()
                .satisfies(v -> assertThat(v.getPropertyPath()).hasToString("exceptionType"));
    }

    @Test
    void blankMessageIsInvalid() {
        ExceptionAnalyzeRequest request = new ExceptionAnalyzeRequest("NullPointerException", "", null);

        Set<ConstraintViolation<ExceptionAnalyzeRequest>> violations = validator.validate(request);

        assertThat(violations).singleElement()
                .satisfies(v -> assertThat(v.getPropertyPath()).hasToString("message"));
    }
}
