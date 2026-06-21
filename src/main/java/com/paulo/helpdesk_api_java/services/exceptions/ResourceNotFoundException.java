package com.paulo.helpdesk_api_java.services.exceptions;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resource, Object id) {
        super("%s não encontrado(a) com identificador: %s".formatted(resource, id));
    }

}
