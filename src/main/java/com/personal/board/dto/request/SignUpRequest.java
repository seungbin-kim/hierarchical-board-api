package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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

  @NotNull(message = "birthday is required.")
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate birthday;

  @NotBlank(message = "password is required.")
  private String password;

}
