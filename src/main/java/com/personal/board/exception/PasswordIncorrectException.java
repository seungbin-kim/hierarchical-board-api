package com.personal.board.exception;

public class PasswordIncorrectException extends BadArgumentException {

  public PasswordIncorrectException() {
    super("password is incorrect.");
  }

}

