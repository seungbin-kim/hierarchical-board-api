package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class BoardRequest {

  @NotBlank(message = "name is required.")
  private String name;

}
