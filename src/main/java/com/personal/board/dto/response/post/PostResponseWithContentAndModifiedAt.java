package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseWithContentAndModifiedAt extends PostResponse {

  private String content;

  private LocalDateTime modifiedAt;

  public PostResponseWithContentAndModifiedAt(final Post post) {
    super(post);
    this.modifiedAt = post.getModifiedAt();
    this.content = post.getContent();
  }

}
