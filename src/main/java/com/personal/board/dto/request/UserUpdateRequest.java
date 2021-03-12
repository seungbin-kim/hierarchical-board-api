package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateRequest {

  @Email(message = "not in email format.")
  private String email;

  private String nickname;

  private String name;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate birthday;

  @NotBlank(message = "password is required.")
  private String password;

  private String newPassword;

}
