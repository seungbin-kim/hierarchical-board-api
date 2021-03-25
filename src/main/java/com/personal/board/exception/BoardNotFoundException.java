package com.personal.board.exception;

public class BoardNotFoundException extends NotFoundException {

  public BoardNotFoundException() {
    super("게시판을 찾을 수 없습니다.");
  }

}
