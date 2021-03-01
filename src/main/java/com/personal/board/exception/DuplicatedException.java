package com.personal.board.exception;

public class DuplicatedException extends RuntimeException {

  public DuplicatedException() {
    super("Email or nickname is duplicated.");
  }
}
