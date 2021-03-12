package com.personal.board.exception;

public class EmailDuplicatedException extends DuplicatedException {

  public EmailDuplicatedException() {
    super("email is duplicated.");
  }

}
