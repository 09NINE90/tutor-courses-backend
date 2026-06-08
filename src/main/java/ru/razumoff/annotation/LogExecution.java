package ru.razumoff.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecution {
    boolean logRequest() default true;
    boolean logResponse() default false;
    boolean logHeaders() default false;
    LogLevel level() default LogLevel.INFO;

    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}