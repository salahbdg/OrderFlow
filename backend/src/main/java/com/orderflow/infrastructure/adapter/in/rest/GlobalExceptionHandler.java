package com.orderflow.infrastructure.adapter.in.rest;

import com.orderflow.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * GLOBAL EXCEPTION HANDLER
 *
 * Catches exceptions thrown anywhere in the application and converts
 * them to proper HTTP responses. This keeps error handling OUT of
 * controllers and in one centralized place.
 *
 * We use ProblemDetail (RFC 7807) — the modern standard for HTTP
 * error responses. Angular can parse this consistently on the frontend.
 *
 * In production you'd also log correlation IDs here so you can
 * trace an error across logs, Kafka events, and database records.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problem.setType(URI.create("/errors/order-not-found"));
        problem.setTitle("Order Not Found");
        return problem;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problem.setType(URI.create("/errors/product-not-found"));
        problem.setTitle("Product Not Found");
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        problem.setType(URI.create("/errors/insufficient-stock"));
        problem.setTitle("Insufficient Stock");
        return problem;
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ProblemDetail handleInvalidState(InvalidOrderStateException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()
        );
        problem.setType(URI.create("/errors/invalid-order-state"));
        problem.setTitle("Invalid Order State Transition");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed"
        );
        problem.setType(URI.create("/errors/validation-failed"));
        problem.setTitle("Validation Error");
        problem.setProperty("errors",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .toList()
        );
        return problem;
    }
}