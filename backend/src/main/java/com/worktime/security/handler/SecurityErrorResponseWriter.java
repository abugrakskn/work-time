package com.worktime.security.handler;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityErrorResponseWriter {
    public void write(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        String responseBody = """
                {
                    "timestamp": "%s",
                    "status": %d,
                    "error": "%s",
                    "message": "%s"
                }
                """.formatted(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        response.getWriter().write(responseBody);
    }
}
