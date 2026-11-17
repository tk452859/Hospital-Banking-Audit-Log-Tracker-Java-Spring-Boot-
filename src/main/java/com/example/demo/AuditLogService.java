package com.example.demo;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service

public class AuditLogService {
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("100000");
    private static final Set<String> CRITICAL_ACTIONS =
            Set.of("DELETE_RECORD", "EXPORT_DATA", "OVERRIDE_LIMIT");

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditLog record(AuditLogRequest request) {
        validate(request);

        AuditLog log = new AuditLog(
                request.userId(), request.role(), request.actionType(), request.amountOrValue());

        String reason = assessRisk(request);
        if (reason != null) {
            log.flag(reason);
        }
        return repository.save(log);
    }

    public List<AuditLog> getFlagged() {
        return repository.findByFlaggedTrueOrderByCreatedAtDesc();
    }

    private void validate(AuditLogRequest r) {
        if (r.userId() == null || r.userId().isBlank()) {
            throw new InvalidAuditLogException("userId is required");
        }
        if (r.role() == null || r.role().isBlank()) {
            throw new InvalidAuditLogException("role is required");
        }
        if (r.actionType() == null || r.actionType().isBlank()) {
            throw new InvalidAuditLogException("actionType is required");
        }
        if (r.amountOrValue() == null || r.amountOrValue().signum() < 0) {
            throw new InvalidAuditLogException("amountOrValue must be zero or positive");
        }
    }

    private String assessRisk(AuditLogRequest r) {
        if (CRITICAL_ACTIONS.contains(r.actionType())) {
            return "Critical action: " + r.actionType();
        }
        if (r.amountOrValue().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            return "High-value transaction: " + r.amountOrValue();
        }
        return null;
    }

}
