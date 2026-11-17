package com.example.demo;

import java.math.BigDecimal;

public record AuditLogRequest(String userId, String role, String actionType, BigDecimal amountOrValue) {
}
