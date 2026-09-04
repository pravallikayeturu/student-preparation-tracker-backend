package com.pravallika.student_preparation_tracker.exception;

import com.pravallika.student_preparation_tracker.dto.ErrorResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles your application/business errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex) {

        // Backend console only
        ex.printStackTrace();

        String message = getSafeMessage(ex.getMessage());

        ErrorResponse response = new ErrorResponse(message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Handles validation errors such as @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Please check your input.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    // Handles database constraint errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Unable to save the data. Please check your input."
                ));
    }

    // Handles other database errors
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            DataAccessException ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Unable to process your request. Please try again later."
                ));
    }

    // Handles every unexpected backend error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex) {

        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Something went wrong. Please try again later."
                ));
    }

    // Only allow safe messages to reach the frontend
    private String getSafeMessage(String message) {

        if (message == null || message.isBlank()) {
            return "Something went wrong. Please try again.";
        }

        if (message.equals("Invalid or expired OTP")) {
            return message;
        }

        if (message.equals("Email already registered")) {
            return message;
        }

        if (message.equals("User not found")) {
            return message;
        }

        if (message.equals("Invalid password")) {
            return message;
        }

        if (message.equals("Current password is incorrect")) {
            return message;
        }

        // Time conflict message
        if (message.equals("Another task already exists at this time")) {
            return message;
        }

        if (message.equals(
                "New password must contain at least 6 characters")) {
            return message;
        }

        if (message.equals(
                "New password must be different from current password")) {
            return message;
        }

        // Any unknown backend error gets hidden
        return "Something went wrong. Please try again.";
    }
}