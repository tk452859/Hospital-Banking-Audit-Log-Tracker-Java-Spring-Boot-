package com.example.demo;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")

public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String role;
    private String actionType;
    private BigDecimal amountOrValue;
    private boolean flagged;
    private String flagReason;
    private LocalDateTime createdAt;

    protected AuditLog() {}

    public AuditLog(String userId, String role, String actionType, BigDecimal amountOrValue) {
        this.userId = userId;
        this.role = role;
        this.actionType = actionType;
        this.amountOrValue = amountOrValue;
        this.createdAt = LocalDateTime.now();
    }

    public void flag(String reason) {
        this.flagged = true;
        this.flagReason = reason;
    }

    // getters and setters
    public boolean isFlagged() {
        return flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public String getActionType() {
        return actionType;
    }
}
