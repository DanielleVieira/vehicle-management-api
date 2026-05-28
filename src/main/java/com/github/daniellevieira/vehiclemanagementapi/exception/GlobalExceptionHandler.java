package com.github.daniellevieira.vehiclemanagementapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var details = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> String.format("%s: %s", field.getField(), field.getDefaultMessage()))
                .toList();
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Validation failed. Please check your request parameters",
                req.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(value = DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                req.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.CONFLICT,
                "Violation of the database's integrity rules",
                req.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage(),
                req.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                req.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter: " + ex.getName(),
                req.getRequestURI(),
                List.of("Parameter '" + ex.getName() + "' must be of type " + ex.getRequiredType().getSimpleName())
        );
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter: Please check your request parameters",
                req.getRequestURI(),
                ex.getConstraintViolations().stream().map(c -> c.getPropertyPath() + ": " + c.getMessage()).toList()
        );
    }

    @ExceptionHandler(value = PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException ex, HttpServletRequest req) {
        return createErrorResponseEntity(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter: Please check your request parameters",
                req.getRequestURI(),
                List.of(ex.getPropertyName() + ": " + ex.getMessage())
        );
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        // TODO registrar log do erro
        return createErrorResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().toString(),
                req.getRequestURI(),
                List.of()
        );
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(
            HttpStatus status,
            String message,
            String uri,
            List<String> details
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        uri,
                        details
                ));
    }
}
