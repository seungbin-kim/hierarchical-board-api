package com.personal.board.dto.response.user;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithCreatedAt extends UserResponse {

  public UserResponseWithCreatedAt(User user) {
    super(user);
    this.createdAt = user.getCreatedAt();
  }

  private final LocalDateTime createdAt;

}
