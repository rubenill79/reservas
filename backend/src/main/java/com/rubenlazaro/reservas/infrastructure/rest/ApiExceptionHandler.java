package com.rubenlazaro.reservas.infrastructure.rest;

import com.rubenlazaro.reservas.domain.BookingNotFoundException;
import com.rubenlazaro.reservas.domain.BookingOverlapException;
import com.rubenlazaro.reservas.domain.InvalidResourceException;
import com.rubenlazaro.reservas.domain.InvalidTimeRangeException;
import com.rubenlazaro.reservas.domain.ResourceAlreadyExistsException;
import com.rubenlazaro.reservas.domain.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Translates domain exceptions into HTTP errors using ProblemDetail (RFC 9457,
 * application/problem+json). The parent class already does the same for Spring MVC's own
 * exceptions: malformed JSON, wrong parameter types, unsupported methods and so on.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    record FieldViolation(String field, String message) {
    }

    @ExceptionHandler({InvalidTimeRangeException.class, InvalidResourceException.class})
    ProblemDetail handleInvalidValue(RuntimeException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class, BookingNotFoundException.class})
    ProblemDetail handleNotFound(RuntimeException e) {
        return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
    }

    @ExceptionHandler({BookingOverlapException.class, ResourceAlreadyExistsException.class})
    ProblemDetail handleConflict(RuntimeException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    /** Bean Validation failures: adds the list of offending fields to the standard problem body. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request", "Validation failed");
        problem.setProperty("errors", errors);
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
