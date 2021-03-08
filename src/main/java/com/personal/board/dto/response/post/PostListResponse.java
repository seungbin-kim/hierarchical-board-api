package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostListResponse extends PostResponse {

  private LocalDateTime createdAt;

  private Long groupId;

  private int groupOrder;

  private int groupDepth;

  private boolean deletedStatus;

  public PostListResponse(final Post post) {
    super(post);
    this.createdAt = post.getCreatedAt();
    this.groupId = post.getGroup().getId();
    this.groupOrder = post.getGroupOrder();
    this.groupDepth = post.getGroupDepth();
    this.deletedStatus = post.isDeleted();
  }

}
