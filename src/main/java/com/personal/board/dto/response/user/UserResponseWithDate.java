package com.personal.board.dto.response.user;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithDate extends UserResponse {

  private final LocalDateTime createdAt;

  private final LocalDateTime modifiedAt;

  public UserResponseWithDate(final User user) {
    super(user);
    this.createdAt = user.getCreatedAt();
    this.modifiedAt = user.getModifiedAt();
  }

}
