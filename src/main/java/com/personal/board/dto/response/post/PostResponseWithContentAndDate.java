package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseWithContentAndDate extends PostResponse {

  private String content;

  private LocalDateTime createdAt;

  private LocalDateTime modifiedAt;

  public PostResponseWithContentAndDate(final Post post) {
    super(post);
    this.content = post.getContent();
    this.createdAt = post.getCreatedAt();
    this.modifiedAt = post.getModifiedAt();
  }

}
