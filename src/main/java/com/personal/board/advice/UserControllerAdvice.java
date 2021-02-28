package com.personal.board.advice;

import com.personal.board.controller.UserController;
import com.personal.board.dto.response.ErrorResponse;
import com.personal.board.exception.DuplicatedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserControllerAdvice {

  @ExceptionHandler(DuplicatedException.class)
  public ResponseEntity<ErrorResponse> emailDuplicatedExceptionHandler(DuplicatedException exception) {
    return
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ErrorType.BAD_ARGUMENT, exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> methodValidExceptionHandler(MethodArgumentNotValidException exception) {
    String defaultMessage =
        Objects.requireNonNull(exception
            .getBindingResult()
            .getFieldError())
            .getDefaultMessage();

    return
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ErrorType.BAD_ARGUMENT, defaultMessage));
  }

}