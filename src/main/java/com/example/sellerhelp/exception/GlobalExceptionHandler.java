package com.example.sellerhelp.exception;

import com.example.sellerhelp.common.ApiResponseDto;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {


    // 401 - Unauthorized
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> unauthorized(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error(e.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    // 403 - Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> forbidden(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.error(e.getMessage(), HttpStatus.FORBIDDEN));
    }


    // 404 - Not Found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> notFound(NoSuchElementException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(e.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> httpMsgNotReadable(HttpMessageNotReadableException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("This Http message must have a body", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> notFound2(NoResourceFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(e.getMessage(), HttpStatus.NOT_FOUND));
    }

    // 409 - Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> conflict(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> invalidResourceAccess(InvalidDataAccessResourceUsageException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error("Could not execute this statement, maybe a resource is missing", HttpStatus.NOT_FOUND));
    }

    // 500 - Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> serverError(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // 400 - Bad Request
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> badRequest(Exception ex) {
        Map<String, Object> errors = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException validEx) {
            errors.put("fields", validEx.getBindingResult().getAllErrors().stream()
                    .collect(Collectors.toMap(
                            err -> ((FieldError) err).getField(),
                            err -> Objects.requireNonNullElse(err.getDefaultMessage(), "Invalid")
                    )));
        } else {
            errors.put("error", ex.getMessage());
        }

        // NO .toBuilder() — just build fresh with data
        ApiResponseDto<Map<String, Object>> response = ApiResponseDto
                .<Map<String, Object>>builder()
                .success(false)
                .message("Invalid request")
                .data(errors)
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(ZonedDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> transactionSystemError(TransactionSystemException e){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(e.getMessage(), HttpStatus.CONFLICT));
    }

    /**
     * Handles common Spring MVC exceptions that occur before the controller method is even called.
     * - NoHandlerFoundException: Endpoint does not exist (e.g., GET /api/nonexistent)
     * - HttpRequestMethodNotSupportedException: Wrong HTTP method (e.g., GET instead of POST)
     * - HttpMediaTypeNotSupportedException: Wrong Content-Type header (e.g., text/plain instead of application/json)
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiResponseDto<Object>> handleMvcExceptions(Exception ex) {
        if (ex instanceof NoHandlerFoundException) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(ex.getMessage(), HttpStatus.NOT_FOUND));
        }
        if (ex instanceof HttpRequestMethodNotSupportedException e) {
            return ResponseEntity
                    .status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(ApiResponseDto.error("HTTP method '" + e.getMethod() + "' is not supported for this endpoint.", HttpStatus.METHOD_NOT_ALLOWED));
        }
        if (ex instanceof HttpMediaTypeNotSupportedException e) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(ApiResponseDto.error("Media type '" + e.getContentType() + "' is not supported. Please use 'application/json'.", HttpStatus.UNSUPPORTED_MEDIA_TYPE));
        }

        // Fallback for any other exceptions in this group
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Invalid request structure.", HttpStatus.BAD_REQUEST));
    }
}