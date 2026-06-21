package com.paulo.helpdesk_api_java.services.exceptions;

import java.io.Serial;

public class TokenGenerationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
