package com.personal.board.dto.response;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithModifiedAt extends UserResponse {

  public UserResponseWithModifiedAt(User user) {
    super(user);
    this.modifiedAt = user.getModifiedAt();
  }

  private final LocalDateTime modifiedAt;

}
