package com.personal.board.exception;

public class NicknameDuplicatedException extends BadArgumentException {

  public NicknameDuplicatedException() {
    super("nickname is duplicated.");
  }

}
