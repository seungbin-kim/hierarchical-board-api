package com.personal.board.exception;

public class BoardNotFoundException extends NotFoundException {

  public BoardNotFoundException() {
    super("board id not found.");
  }

}
