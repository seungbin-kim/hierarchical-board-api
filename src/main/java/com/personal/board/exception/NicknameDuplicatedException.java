package com.personal.board.exception;

public class NicknameDuplicatedException extends DuplicatedException {

  public NicknameDuplicatedException() {
    super("nickname is duplicated.");
  }

}
