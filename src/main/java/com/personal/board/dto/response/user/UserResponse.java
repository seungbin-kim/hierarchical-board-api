package com.personal.board.dto.response.user;

import com.personal.board.entity.User;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserResponse {

  private final Long id;

  private final String email;

  private final String nickname;

  private final String name;

  private final LocalDate birthday;

  public UserResponse(final User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.nickname = user.getNickname();
    this.name = user.getName();
    this.birthday = user.getBirthday();
  }

}
