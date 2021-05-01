package com.personal.board.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class PostUpdateRequest {

  private String title;

  private String content;

}
