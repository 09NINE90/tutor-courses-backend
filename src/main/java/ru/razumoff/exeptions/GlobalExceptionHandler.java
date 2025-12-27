package ru.razumoff.exeptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.razumoff.commonlib.dto.ApiError;
import ru.razumoff.commonlib.exceptions.ErrorCode;
import ru.razumoff.commonlib.exceptions.PlatformException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlatformException.class)
    public ResponseEntity<ApiError> handlePlatformException(PlatformException ex,
                                                            HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        String message = ex.getCustomMessage() != null
                ? ex.getCustomMessage()
                : code.getDefaultMessage();

        ApiError error = new ApiError(
                Instant.now(),
                code.getHttpStatus(),
                code.getCode(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(code.getHttpStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex,
                                                  HttpServletRequest request) {
        ApiError error = new ApiError(
                Instant.now(),
                500,
                ErrorCode.INTERNAL_ERROR.getCode(),
                "Internal server error",
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(error);
    }
}

