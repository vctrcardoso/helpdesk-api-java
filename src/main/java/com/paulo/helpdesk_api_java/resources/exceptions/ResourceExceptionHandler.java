package com.paulo.helpdesk_api_java.resources.exceptions;

import com.paulo.helpdesk_api_java.services.exceptions.BusinessRuleException;
import com.paulo.helpdesk_api_java.services.exceptions.InvalidRefreshTokenException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import com.paulo.helpdesk_api_java.services.exceptions.TokenGenerationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class ResourceExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<StandardError> handleResourceConflict(
            ResourceConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<StandardError> handleBusinessRule(
            BusinessRuleException exception, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, exception.getCode(), exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new ValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        sanitizeRejectedValue(error)))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Um ou mais campos são inválidos.", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                "Um ou mais parâmetros são inválidos.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleUnreadableMessage(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        String message = "O corpo da requisição está ausente ou contém JSON inválido.";
        Throwable cause = exception.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormat && !invalidFormat.getPath().isEmpty()) {
            String field = invalidFormat.getPath().getLast().getPropertyName();
            Class<?> targetType = invalidFormat.getTargetType();
            if (targetType != null && targetType.isEnum()) {
                message = "Valor inválido para '%s'. Valores aceitos: %s."
                        .formatted(field, Arrays.toString(targetType.getEnumConstants()));
            } else {
                message = "O campo '%s' possui um tipo ou formato inválido.".formatted(field);
            }
        }

        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY", message, request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = "O parâmetro '%s' recebeu o valor inválido '%s'."
                .formatted(exception.getName(), exception.getValue());
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleDataIntegrity(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        LOGGER.warn("Violação de integridade de dados em {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "A operação conflita com dados existentes ou relacionados.", request, List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> handleBadCredentials(
            BadCredentialsException exception, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "E-mail ou senha inválidos.", request, List.of());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<StandardError> handleInvalidRefreshToken(
            InvalidRefreshTokenException exception, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN",
                exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "Você não possui permissão para executar esta operação.", request, List.of());
    }

    @ExceptionHandler(TokenGenerationException.class)
    public ResponseEntity<StandardError> handleTokenGeneration(
            TokenGenerationException exception, HttpServletRequest request) {
        LOGGER.error("Falha ao gerar token", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_GENERATION_ERROR",
                "Não foi possível concluir a autenticação.", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        LOGGER.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocorreu um erro interno inesperado.", request, List.of());
    }

    private ResponseEntity<StandardError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ValidationError> validationErrors) {
        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(error);
    }

    private Object sanitizeRejectedValue(FieldError error) {
        return "password".equalsIgnoreCase(error.getField()) ? null : error.getRejectedValue();
    }
}
