package com.campus360.platform.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Detects if the incoming request expects a Server-Sent Events stream.
     * When true the handler must NOT attempt to write a JSON body — the SSE
     * connection has a fixed {@code text/event-stream} content negotiation and
     * Spring will throw {@code HttpMediaTypeNotAcceptableException} when it tries
     * to serialize a {@link ResponseEntity<ApiError>} for that media type.
     */
    private static boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    /**
     * Writes a structured SSE error event and completes the stream cleanly.
     * This prevents the secondary HttpMediaTypeNotAcceptableException that occurs
     * when Spring tries to write a JSON ApiError body into an SSE response.
     */
    private static void writeSseError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            String payload = "event: error\ndata: " + message.replace("\n", " ") + "\n\n";
            response.getWriter().write(payload);
            response.getWriter().flush();
        } catch (IOException ignored) {
            // Client already disconnected — nothing more to do
        }
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex, HttpServletRequest request, HttpServletResponse response) {
        String correlationId = UUID.randomUUID().toString();
        if (ex.getStatus().is5xxServerError()) {
            log.error("[{}] ApiException: {}", correlationId, ex.getMessage(), ex);
        } else {
            log.warn("[{}] ApiException: {}", correlationId, ex.getMessage());
        }
        // SSE clients cannot receive a JSON body — write an SSE error event instead
        if (isSseRequest(request)) {
            writeSseError(response, ex.getStatus().value(), ex.getMessage());
            return null;
        }
        ApiError error = new ApiError(
                ex.getStatus().value(),
                ex.getStatus().name(),
                ex.getMessage(),
                request.getRequestURI(),
                correlationId,
                null
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request, HttpServletResponse response) {
        String correlationId = UUID.randomUUID().toString();
        List<ValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationError(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());
        if (isSseRequest(request)) {
            writeSseError(response, HttpStatus.BAD_REQUEST.value(), "Request validation failed");
            return null;
        }
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                correlationId,
                fieldErrors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request, HttpServletResponse response) {
        String correlationId = UUID.randomUUID().toString();
        if (isSseRequest(request)) {
            writeSseError(response, HttpStatus.FORBIDDEN.value(), "Access denied");
            return null;
        }
        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "Access denied",
                request.getRequestURI(),
                correlationId,
                null
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                "Resource not found",
                request.getRequestURI(),
                UUID.randomUUID().toString(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Client disconnected before the server finished writing the response.
     * This is a normal network event (browser navigation, closed tab, timeout).
     * Returning null suppresses Spring MVC's attempt to write a secondary error
     * response to the already-dead socket.
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public ResponseEntity<Void> handleClientDisconnect(AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected before response completed: {}", ex.getMessage());
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request, HttpServletResponse response) {
        // Suppress connection-abort errors — socket is already dead, writing would throw again
        if (isClientAbortException(ex)) {
            log.debug("Client connection aborted: {}", request.getRequestURI());
            return null;
        }
        // SSE clients cannot receive a JSON body
        if (isSseRequest(request)) {
            log.error("Unhandled exception on SSE endpoint {}: {}", request.getRequestURI(), ex.getMessage(), ex);
            writeSseError(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
            return null;
        }
        String correlationId = UUID.randomUUID().toString();
        log.error("[{}] Unhandled exception", correlationId, ex);
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                correlationId,
                null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** Checks the full cause chain for client-initiated disconnection signals. */
    private boolean isClientAbortException(Throwable ex) {
        Throwable cause = ex;
        for (int depth = 0; cause != null && depth < 10; depth++) {
            if (cause instanceof AsyncRequestNotUsableException) return true;
            if (cause instanceof IOException) {
                String msg = cause.getMessage();
                if (msg != null && (
                        msg.contains("An established connection was aborted") ||
                        msg.contains("Connection reset by peer") ||
                        msg.contains("Broken pipe"))) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
