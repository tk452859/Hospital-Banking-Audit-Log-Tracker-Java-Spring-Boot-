package com.example.demo;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice

public class AuditExceptionHandler {
    @ExceptionHandler(InvalidAuditLogException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(InvalidAuditLogException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

}
