package com.paulo.helpdesk_api_java.services.exceptions;

import java.io.Serial;

public class ResourceConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;

    public ResourceConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
