package com.personal.board.exception;

public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException() {
    super("사용자가 존재하지 않습니다.");
  }

}
