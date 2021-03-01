package com.personal.board.exception;

public class PasswordIncorrectException extends RuntimeException {

  public PasswordIncorrectException() {
    super("Password is incorrect.");
  }

}

