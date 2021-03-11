package com.personal.board.exception;

public class PostNotFoundException extends NotFoundException {

  public PostNotFoundException() {
    super("post id not found");
  }

}
