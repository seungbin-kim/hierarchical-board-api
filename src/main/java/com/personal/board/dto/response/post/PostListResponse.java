package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostListResponse extends PostResponse {

  private LocalDateTime createdAt;

  private Long parentId;

  private boolean deletedStatus;

  private List<PostListResponse> reply;

  public PostListResponse(final Post post) {
    super(post);
    this.createdAt = post.getCreatedAt();
    if (post.getParent() != null) {
      this.parentId = post.getParent().getId();
    }
    this.deletedStatus = post.isDeleted();
    this.reply = post.getChildren()
        .stream()
        .map(PostListResponse::new)
        .collect(Collectors.toList());
  }

}
