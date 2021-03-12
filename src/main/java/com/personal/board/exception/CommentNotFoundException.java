package com.personal.board.exception;

public class CommentNotFoundException extends NotFoundException {

  public CommentNotFoundException() {
    super("comment id not found.");
  }

}
