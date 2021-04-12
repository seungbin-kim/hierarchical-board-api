package com.personal.board.dto.response.comment;

import com.personal.board.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponse {

  private Long id;

  private String userNickname;

  private String content;

  public CommentResponse(final Comment comment) {
    this.id = comment.getId();
    this.userNickname = comment.getUser().getNickname();
    this.content = comment.getContent();
  }

}
