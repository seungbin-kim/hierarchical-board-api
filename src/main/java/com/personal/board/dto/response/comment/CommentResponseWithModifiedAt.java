package com.personal.board.dto.response.comment;

import com.personal.board.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseWithModifiedAt extends CommentResponse {

  private LocalDateTime modifiedAt;

  public CommentResponseWithModifiedAt(final Comment comment) {
    super(comment);
    this.modifiedAt = comment.getModifiedAt();
  }

}
