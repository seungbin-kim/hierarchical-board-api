package com.personal.board.advice;

import com.personal.board.dto.response.ErrorResponse;
import com.personal.board.enumeration.ErrorType;
import com.personal.board.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Set;

@RestControllerAdvice(basePackages = "com.personal.board.controller")
public class ControllerExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> notFoundExceptionHandler(final NotFoundException exception) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.NOT_FOUND, exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> methodValidExceptionHandler(final MethodArgumentNotValidException exception) {
    String defaultMessage =
        Objects.requireNonNull(exception
            .getBindingResult()
            .getFieldError())
            .getDefaultMessage();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.BAD_ARGUMENT, defaultMessage));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> constraintViolationException(final ConstraintViolationException exception) {
    Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
    StringBuilder stringBuilder = new StringBuilder();
    constraintViolations.iterator()
        .forEachRemaining(constraintViolation -> {
          stringBuilder.append(constraintViolation.getMessage());
        });

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.BAD_ARGUMENT, stringBuilder.toString()));
  }

  @ExceptionHandler(BadArgumentException.class)
  public ResponseEntity<ErrorResponse> badArgumentExceptionHandler(final BadArgumentException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  @ExceptionHandler(DuplicatedException.class)
  public ResponseEntity<ErrorResponse> duplicatedExceptionHandler(final DuplicatedException exception) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.BAD_ARGUMENT, "incorrect data value."));
  }

  @ExceptionHandler(ReflectIllegalAccessException.class)
  public ResponseEntity<ErrorResponse> reflectIllegalAccessExceptionHandler(final ReflectIllegalAccessException exception) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.SERVER_ERROR, exception.getMessage()));
  }

  @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
  public ResponseEntity<ErrorResponse> authenticationExceptionHandler() {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.FORBIDDEN, "Authentication error."));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> runtimeExceptionHandler(final RuntimeException exception) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GlobalExceptionHandler.makeErrorResponse(ErrorType.SERVER_ERROR, exception.getMessage()));
  }

}
