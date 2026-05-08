package com.sena.springpoo.controller;

import com.sena.springpoo.exceptions.BadRequestException;
import com.sena.springpoo.exceptions.DatabaseException;
import com.sena.springpoo.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        logger.warn("[400 Bad Request] {}", ex.getMessage());
        return buildJson(400, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        logger.warn("[400 Bad Request] Parámetro faltante: {}", ex.getMessage());
        return buildJson(400, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        logger.warn("[404 Not Found] {}", ex.getMessage());
        return buildJson(404, ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public void handleNoHandler(HttpServletResponse response) throws IOException {
        logger.warn("[404 Not Found] Ruta no encontrada → redirigiendo a error.html");
        response.sendRedirect("/error.html?status=404");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResource(HttpServletResponse response) throws IOException {
        logger.warn("[404 Not Found] Recurso estático no encontrado → redirigiendo a error.html");
        response.sendRedirect("/error.html?status=404");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        logger.warn("[405 Method Not Allowed] {}", ex.getMessage());
        return buildJson(405, ex.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabase(DatabaseException ex) {
        logger.error("[500 Database Error] {} | Causa: {}",
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause().getMessage() : "desconocida",
                ex);
        return buildJson(500, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        logger.error("[500 Error Inesperado] {} | Tipo: {}", ex.getMessage(), ex.getClass().getName(), ex);
        return buildJson(500, "Error interno del servidor: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildJson(int status, String mensaje) {
        Map<String, Object> body = Map.of(
                "status", status,
                "mensaje", mensaje
        );
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(body);
    }
}
