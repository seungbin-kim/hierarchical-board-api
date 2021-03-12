package com.personal.board.dto.response.user;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithModifiedAt extends UserResponse {

  private final LocalDateTime modifiedAt;

  public UserResponseWithModifiedAt(final User user) {
    super(user);
    this.modifiedAt = user.getModifiedAt();
  }

}
