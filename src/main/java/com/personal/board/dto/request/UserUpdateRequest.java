package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UserUpdateRequest {

  @Email(message = "Not in email format.")
  private String email;

  private String nickname;

  private String name;

  private int age;

  @NotBlank(message = "Password is required.")
  private String password;

  private String newPassword;

}
