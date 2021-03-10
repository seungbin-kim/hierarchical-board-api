package com.personal.board.dto.response.comment;

import com.personal.board.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseWithCreatedAt extends CommentResponse {

  public CommentResponseWithCreatedAt(Comment comment) {
    super(comment);
    this.createdAt = comment.getCreatedAt();
  }

  private LocalDateTime createdAt;

}
