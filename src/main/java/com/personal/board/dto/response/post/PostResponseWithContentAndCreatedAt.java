package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseWithContentAndCreatedAt extends PostResponse {

  private String content;

  private LocalDateTime createdAt;

  public PostResponseWithContentAndCreatedAt(final Post post) {
    super(post);
    this.createdAt = post.getCreatedAt();
    this.content = post.getContent();
  }

}
