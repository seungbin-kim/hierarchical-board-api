package com.personal.board.advice;

import com.personal.board.controller.UserController;
import com.personal.board.dto.response.ErrorResponse;
import com.personal.board.exception.DuplicatedException;
import com.personal.board.exception.NotFoundException;
import com.personal.board.exception.PasswordIncorrectException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserControllerAdvice {

  @ExceptionHandler(DuplicatedException.class)
  public ResponseEntity<ErrorResponse> duplicatedExceptionHandler(DuplicatedException exception) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(makeErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> notFoundExceptionHandler(NotFoundException exception) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(makeErrorResponse(ErrorType.NOT_FOUND, exception.getMessage()));
  }

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

  @ExceptionHandler(PasswordIncorrectException.class)
  public ResponseEntity<ErrorResponse> passwordIncorrectExceptionHandler(PasswordIncorrectException exception) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(makeErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  private ErrorResponse makeErrorResponse(ErrorType errorType, String message) {
    return new ErrorResponse(errorType, message);
  }

}