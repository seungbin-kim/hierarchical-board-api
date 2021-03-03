package com.personal.board.exception;

public class NameDuplicatedException extends BadArgumentException {

  public NameDuplicatedException() {
    super("name is duplicated.");
  }
}
