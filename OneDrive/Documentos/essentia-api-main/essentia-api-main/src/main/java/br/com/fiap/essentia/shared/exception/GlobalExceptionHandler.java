package br.com.fiap.essentia.shared.exception;

import br.com.fiap.essentia.shared.exception.Exceptions.BusinessException;
import br.com.fiap.essentia.shared.exception.Exceptions.EmailAlreadyInUseException;
import br.com.fiap.essentia.shared.exception.Exceptions.EntityNotFoundException;
import br.com.fiap.essentia.shared.exception.Exceptions.InvalidPasswordException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {}

    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path, Map<String,String> fieldErrors) {
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                (fieldErrors == null || fieldErrors.isEmpty()) ? null : fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }

    // 400 – validação de DTO (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Erro de validação", request.getDescription(false), errors);
    }

    // 400 – parâmetro obrigatório ausente
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName(), request.getDescription(false), null);
    }

    // 400 – senha inválida (sua policy)
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPassword(InvalidPasswordException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(false), null);
    }

    // 401 – credenciais inválidas (login)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request.getDescription(false), null);
    }

    // 403 – sem permissão
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", request.getDescription(false), null);
    }

    // 404 – não encontrado (domínio)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false), null);
    }

    // 409 – e-mail em uso / conflitos de negócio
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailConflict(EmailAlreadyInUseException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getDescription(false), null);
    }

    // 409 – regra de negócio violada
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request.getDescription(false), null);
    }

    // 400 – argumentos inválidos genéricos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArg(IllegalArgumentException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(false), null);
    }

    // 500 – fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", request.getDescription(false), null);
    }
}
