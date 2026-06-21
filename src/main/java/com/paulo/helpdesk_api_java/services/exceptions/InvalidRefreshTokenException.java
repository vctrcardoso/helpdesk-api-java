package com.paulo.helpdesk_api_java.services.exceptions;

import java.io.Serial;

public class InvalidRefreshTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
