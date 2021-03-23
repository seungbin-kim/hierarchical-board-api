package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SignInRequest {

  @Email(message = "not in email format.")
  @NotBlank(message = "email is required")
  private String email;

  @NotBlank(message = "password is required.")
  private String password;

}
