package com.example.demo.controller;

import java.util.NoSuchElementException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", mensagemSegura(
            ex.getMessage(),
            "Dados inválidos. Verifique as informações enviadas."
        )));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Dados inválidos.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> bind(BindException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", "Informe números válidos nos campos do formulário."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> invalidJson(HttpMessageNotReadableException ex) {
        logger.warn("Requisição com JSON inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", "Dados inválidos. Verifique as informações enviadas."));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", "Registro não encontrado."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrity(DataIntegrityViolationException ex) {
        logger.error("Falha de integridade no banco de dados", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "Não foi possível concluir a operação. Verifique os dados e tente novamente."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> fileTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(Map.of("message", "Arquivo muito grande. Envie uma imagem menor."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Acesso negado."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex) {
        logger.error("Erro inesperado na API", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Erro interno. Tente novamente mais tarde."));
    }

    private String mensagemSegura(String message, String fallback) {
        if (message == null || message.isBlank()) return fallback;

        String lower = message.toLowerCase();
        boolean tecnica = lower.contains("exception")
            || lower.contains("java.")
            || lower.contains("org.springframework")
            || lower.contains("failed to convert")
            || lower.contains("could not")
            || lower.contains("constraint")
            || lower.contains("foreign key")
            || lower.contains("sql")
            || lower.contains("stacktrace");

        return tecnica ? fallback : message;
    }
}
