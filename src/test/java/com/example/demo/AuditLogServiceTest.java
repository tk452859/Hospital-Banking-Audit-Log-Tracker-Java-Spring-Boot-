package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class AuditLogServiceTest {
    @Mock
    private AuditLogRepository repository;

    @InjectMocks
    private AuditLogService service;

    @BeforeEach
    void setUp() {
        lenient().when(repository.save(any(AuditLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void normalLogIsSavedWithoutFlag() {
        AuditLog result = service.record(
                new AuditLogRequest("U1", "TELLER", "VIEW_BALANCE", new BigDecimal("500")));

        assertFalse(result.isFlagged());
        assertNull(result.getFlagReason());
    }

    @Test
    void highValueTransactionIsFlagged() {
        AuditLog result = service.record(
                new AuditLogRequest("U2", "MANAGER", "TRANSFER", new BigDecimal("250000")));

        assertTrue(result.isFlagged());
        assertTrue(result.getFlagReason().startsWith("High-value"));
    }

    @Test
    void criticalActionIsFlagged() {
        AuditLog result = service.record(
                new AuditLogRequest("U3", "ADMIN", "DELETE_RECORD", BigDecimal.ZERO));

        assertTrue(result.isFlagged());
        assertTrue(result.getFlagReason().contains("DELETE_RECORD"));
    }

    @Test
    void blankUserIdIsRejected() {
        assertThrows(InvalidAuditLogException.class, () ->
                service.record(new AuditLogRequest(" ", "TELLER", "VIEW", BigDecimal.ONE)));
        verify(repository, never()).save(any());
    }

    @Test
    void negativeAmountIsRejected() {
        assertThrows(InvalidAuditLogException.class, () ->
                service.record(new AuditLogRequest("U4", "TELLER", "WITHDRAW", new BigDecimal("-10"))));
    }

    @Test
    void getFlaggedReturnsOnlyRepositoryFlaggedResults() {
        AuditLog flaggedLog = new AuditLog("U5", "ADMIN", "EXPORT_DATA", BigDecimal.ZERO);
        flaggedLog.flag("Critical action: EXPORT_DATA");
        when(repository.findByFlaggedTrueOrderByCreatedAtDesc()).thenReturn(List.of(flaggedLog));

        List<AuditLog> result = service.getFlagged();

        assertEquals(1, result.size());
        assertEquals("EXPORT_DATA", result.get(0).getActionType());
    }

}
