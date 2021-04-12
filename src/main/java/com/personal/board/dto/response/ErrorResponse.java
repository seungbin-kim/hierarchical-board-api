package com.personal.board.dto.response;

import com.personal.board.enumeration.ErrorType;
import lombok.Getter;

@Getter
public class ErrorResponse {

  private final Error error;

  public ErrorResponse(final ErrorType errorType, final String message) {
    this.error = new Error(errorType.toString(), message);
  }

  @Getter
  static class Error {

    private final String type;

    private final String message;

    public Error(final String type, final String message) {

      this.type = type;
      this.message = message;
    }

  }

}
