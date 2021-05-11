package com.personal.board.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
public class CommentRequest {

  private Long parentId;

  @NotBlank(message = "content is required.")
  private String content;

}
