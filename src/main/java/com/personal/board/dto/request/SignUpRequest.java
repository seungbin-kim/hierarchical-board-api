package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SignUpRequest {

  @Email(message = "Not in email format.")
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Nickname is required.")
  private String nickname;

  @NotBlank(message = "Name is required.")
  private String name;

  private int age;

  @NotBlank(message = "Password is required.")
  private String password;

}
