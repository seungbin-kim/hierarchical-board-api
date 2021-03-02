package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SignUpRequest {

  @Email(message = "not in email format.")
  @NotBlank(message = "email is required")
  private String email;

  @NotBlank(message = "nickname is required.")
  private String nickname;

  @NotBlank(message = "name is required.")
  private String name;

  @NotNull(message = "age is required.")
  private Integer age;

  @NotBlank(message = "password is required.")
  private String password;

}
