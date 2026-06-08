package ru.razumoff.annotation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(logExecution)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        if (logExecution.logRequest()) {
            logRequest(requestId, joinPoint, request, logExecution);
        }

        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            logResponse(requestId, result, exception, executionTime, logExecution);
        }
    }

    private void logRequest(String requestId, ProceedingJoinPoint joinPoint,
                            HttpServletRequest request, LogExecution logExecution) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(System.lineSeparator());
        logBuilder.append("--------------------------------------------------").append(System.lineSeparator());
        logBuilder.append("REQUEST [").append(requestId).append("]").append(System.lineSeparator());
        logBuilder.append("Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append(System.lineSeparator());
        logBuilder.append("Method: ").append(joinPoint.getSignature().toShortString()).append(System.lineSeparator());

        if (request != null) {
            logBuilder.append("HTTP Method: ").append(request.getMethod()).append(System.lineSeparator());
            logBuilder.append("URL: ").append(request.getRequestURL()).append(System.lineSeparator());
            logBuilder.append("Remote IP: ").append(getClientIp(request)).append(System.lineSeparator());

            if (logExecution.logHeaders()) {
                logBuilder.append("Headers: ").append(getHeadersAsString(request)).append(System.lineSeparator());
            }
        }

        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            String argsStr = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest) && !(arg instanceof HttpServletResponse))
                    .map(arg -> {
                        try {
                            return objectMapper.writeValueAsString(arg);
                        } catch (Exception e) {
                            return String.valueOf(arg);
                        }
                    })
                    .collect(Collectors.joining(", "));
            logBuilder.append("Arguments: ").append(argsStr).append(System.lineSeparator());
        }

        logBuilder.append("--------------------------------------------------");

        logExecutionLevel(logExecution, logBuilder.toString());
    }

    private void logResponse(String requestId, Object result, Exception exception,
                             long executionTime, LogExecution logExecution) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(System.lineSeparator());
        logBuilder.append("--------------------------------------------------").append(System.lineSeparator());
        logBuilder.append("RESPONSE [").append(requestId).append("]").append(System.lineSeparator());
        logBuilder.append("Execution Time: ").append(executionTime).append(" ms").append(System.lineSeparator());

        if (exception != null) {
            logBuilder.append("Exception: ").append(exception.getClass().getSimpleName()).append(System.lineSeparator());
            logBuilder.append("Message: ").append(exception.getMessage()).append(System.lineSeparator());
            logExecutionLevel(logExecution, logBuilder.toString(), true);
        } else if (logExecution.logResponse() && result != null) {
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > 1000) {
                    resultStr = resultStr.substring(0, 1000) + "... [TRUNCATED]";
                }
                logBuilder.append("Response: ").append(resultStr).append(System.lineSeparator());
                logExecutionLevel(logExecution, logBuilder.toString());
            } catch (Exception e) {
                logBuilder.append("Response: ").append(String.valueOf(result)).append(System.lineSeparator());
                logExecutionLevel(logExecution, logBuilder.toString());
            }
        } else {
            logExecutionLevel(logExecution, logBuilder.toString());
        }

        logBuilder.append("--------------------------------------------------");
    }

    private void logExecutionLevel(LogExecution logExecution, String message) {
        logExecutionLevel(logExecution, message, false);
    }

    private void logExecutionLevel(LogExecution logExecution, String message, boolean isError) {
        switch (logExecution.level()) {
            case DEBUG:
                log.debug(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case ERROR:
                log.error(message);
                break;
            default:
                if (isError) {
                    log.error(message);
                } else {
                    log.info(message);
                }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getHeadersAsString(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
    }
}