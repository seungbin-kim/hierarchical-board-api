package com.personal.board.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CommentRequest {

  private Long parentId;

  @NotNull(message = "user id is required.")
  private Long writerId;

  @NotBlank(message = "content is required.")
  private String content;

}
