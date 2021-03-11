package com.personal.board.dto.response.comment;

import com.personal.board.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentListResponse extends CommentResponse {

  private LocalDateTime createdAt;

  private Long parentId;

  private boolean deletedStatus;

  private List<CommentResponse> reply = new ArrayList<>();

  public CommentListResponse(final Comment comment) {
    super(comment);
    this.createdAt = comment.getCreatedAt();
    if (comment.getParent() != null) {
      this.parentId = comment.getParent().getId();
    }
    this.deletedStatus = comment.isDeleted();
    this.reply = comment.getChildren()
    .stream()
    .map(CommentListResponse::new)
    .collect(Collectors.toList());
  }

}
