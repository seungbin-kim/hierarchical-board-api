package com.personal.board.dto.response.comment;

import com.personal.board.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponse {

  public CommentResponse(Comment comment) {
    this.id = comment.getId();
    this.userNickname = comment.getUser().getNickname();
    this.content = comment.getContent();
  }

  private Long id;

  private String userNickname;

  private String content;

}
