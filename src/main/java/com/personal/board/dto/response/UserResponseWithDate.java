package com.personal.board.dto.response;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseWithDate extends UserResponse {

  public UserResponseWithDate(User user) {
    super(user);
    this.createdAt = user.getCreatedAt();
    this.modifiedAt = user.getModifiedAt();
  }

  private final LocalDateTime createdAt;

  private final LocalDateTime modifiedAt;

}
