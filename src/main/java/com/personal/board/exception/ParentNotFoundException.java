package com.personal.board.exception;

public class ParentNotFoundException extends NotFoundException {

  public ParentNotFoundException() {
    super("parent id not found.");
  }

}
