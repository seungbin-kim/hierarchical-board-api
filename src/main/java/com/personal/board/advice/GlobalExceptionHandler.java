package com.personal.board.advice;

import com.personal.board.dto.response.ErrorResponse;
import com.personal.board.enumeration.ErrorType;
import com.personal.board.exception.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class, JwtException.class})
  public ResponseEntity<ErrorResponse> authenticationExceptionHandler() {
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(makeErrorResponse(ErrorType.FORBIDDEN, "Authentication error."));
  }

  static ErrorResponse makeErrorResponse(final ErrorType errorType, final String message) {
    return new ErrorResponse(errorType, message);
  }

}
