package com.personal.board.advice;

import com.personal.board.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.personal.board.controller")
public class GlobalControllerAdvice {

  @ExceptionHandler(NullPointerException.class)
  public ResponseEntity<ErrorResponse> nullPointerExceptionHandler(NullPointerException exception) {
    return
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ErrorType.SERVER_ERROR, exception.getMessage()));
  }

}
