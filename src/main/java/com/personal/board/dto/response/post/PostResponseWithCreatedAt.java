package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseWithCreatedAt extends PostResponse {

  public PostResponseWithCreatedAt(final Post post) {
    super(post);
    this.createdAt = post.getCreatedAt();
  }

  private LocalDateTime createdAt;

}
