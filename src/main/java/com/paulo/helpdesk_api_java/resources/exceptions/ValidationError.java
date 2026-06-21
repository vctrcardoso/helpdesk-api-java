package com.paulo.helpdesk_api_java.resources.exceptions;

public record ValidationError(
        String field,
        String message,
        Object rejectedValue
) {
}
