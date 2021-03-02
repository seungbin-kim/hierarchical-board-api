package com.personal.board.advice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.personal.board.dto.response.ErrorResponse;
import com.personal.board.enumeration.ErrorType;
import com.personal.board.exception.BadArgumentException;
import com.personal.board.exception.ReflectIllegalAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice(basePackages = "com.personal.board.controller")
public class GlobalControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> methodValidExceptionHandler(MethodArgumentNotValidException exception) {
    String defaultMessage =
        Objects.requireNonNull(exception
            .getBindingResult()
            .getFieldError())
            .getDefaultMessage();

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ErrorType.BAD_ARGUMENT, defaultMessage));
  }

  @ExceptionHandler(BadArgumentException.class)
  public ResponseEntity<ErrorResponse> badArgumentExceptionHandler(BadArgumentException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(makeErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  @ExceptionHandler(InvalidFormatException.class)
  public ResponseEntity<ErrorResponse> InvalidFormatExceptionHandler() {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ErrorType.BAD_ARGUMENT, "incorrect data type."));
  }

  @ExceptionHandler(ReflectIllegalAccessException.class)
  public ResponseEntity<ErrorResponse> updateExceptionHandler(ReflectIllegalAccessException exception) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorType.SERVER_ERROR, exception.getMessage()));
  }

  private ErrorResponse makeErrorResponse(ErrorType errorType, String message) {
    return new ErrorResponse(errorType, message);
  }

}
