package com.personal.board.exception;

public class NameDuplicatedException extends DuplicatedException {

  public NameDuplicatedException() {
    super("name is duplicated.");
  }

}
