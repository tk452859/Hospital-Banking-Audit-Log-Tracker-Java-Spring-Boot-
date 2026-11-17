package com.example.demo;

public class InvalidAuditLogException extends RuntimeException{
    public InvalidAuditLogException(String message) {
        super(message);
    }

}
