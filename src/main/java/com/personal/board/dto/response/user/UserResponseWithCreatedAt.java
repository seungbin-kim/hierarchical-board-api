package com.personal.board.dto.response.user;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithCreatedAt extends UserResponse {

  private final LocalDateTime createdAt;

  public UserResponseWithCreatedAt(final User user) {
    super(user);
    this.createdAt = user.getCreatedAt();
  }

}
