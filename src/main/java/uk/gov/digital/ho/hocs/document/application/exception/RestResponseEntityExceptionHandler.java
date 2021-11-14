package uk.gov.digital.ho.hocs.document.application.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.STACKTRACE;
import static uk.gov.digital.ho.hocs.document.application.LogEvent.UNCAUGHT_EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {


    @ExceptionHandler(ApplicationExceptions.DocumentFormatException.class)
    public ResponseEntity handle(ApplicationExceptions.DocumentFormatException e) {
        log.error("ApplicationExceptions.DocumentFormatException: {}", e.getMessage(), value(EVENT, e.getEvent()), value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationExceptions.DocumentConversionException.class)
    public ResponseEntity handle(ApplicationExceptions.DocumentConversionException e) {
        log.error("ApplicationExceptions.DocumentConversionException: {}", e.getMessage(), value(EVENT, e.getEvent()), value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handle(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity handle(HttpMessageConversionException e) {
        log.error("HttpMessageConversionException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity handle(UnsupportedOperationException e) {
        log.error("UnsupportedOperationException: {}", e.getMessage(), value(EVENT, METHOD_NOT_ALLOWED));
        return new ResponseEntity<>(e.getMessage(), METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handle(Exception e) {
        Writer stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        log.error("Exception: {}, Event: {}, Stack: {}", e.getMessage(), value(EVENT, UNCAUGHT_EXCEPTION),
                value(STACKTRACE, stackTraceWriter.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
