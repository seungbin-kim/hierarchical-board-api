package com.personal.board.exception;

public class UserNotFoundException extends NotFoundException {

  public UserNotFoundException() {
    super("user id not found.");
  }

}
