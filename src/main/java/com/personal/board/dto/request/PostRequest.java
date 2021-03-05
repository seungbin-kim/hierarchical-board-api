package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostRequest {

  private Long parentId;

  @NotNull(message = "user id is required.")
  private Long writerId;

  @NotBlank(message = "title is required.")
  private String title;

  @NotBlank(message = "content is required.")
  private String content;

}
