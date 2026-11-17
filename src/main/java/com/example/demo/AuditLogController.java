package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")

public class AuditLogController {
    private final AuditLogService service;

    public AuditLogController(AuditLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AuditLog> create(@RequestBody AuditLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.record(request));
    }

    @GetMapping("/flagged")
    public List<AuditLog> flagged() {
        return service.getFlagged();
    }
}


