package org.example.employeepayroll.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.example.employeepayroll.dtos.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                                            .message(ex.getMessage())
                                            .status(ex.getStatusCode().value())
                                            .timestamp(LocalDateTime.now())
                                            .error(Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value())).getReasonPhrase())
                                            .path(request.getRequestURI())
                                            .build();

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String fieldErrors = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(fieldErrors)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message("Invalid username or password")
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        //TODO: Add logging instead of this
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
