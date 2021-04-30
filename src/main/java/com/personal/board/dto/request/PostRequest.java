package com.personal.board.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@EqualsAndHashCode
public class PostRequest {

  private Long parentId;

  @NotBlank(message = "title is required.")
  private String title;

  @NotBlank(message = "content is required.")
  private String content;

}
