package com.personal.board.exception;

public class EmailDuplicatedException extends BadArgumentException {

  public EmailDuplicatedException() {
    super("email is duplicated.");
  }

}
