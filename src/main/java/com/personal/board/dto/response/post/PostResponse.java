package com.personal.board.dto.response.post;

import com.personal.board.entity.Post;
import lombok.Getter;

@Getter
public class PostResponse {

  public PostResponse(final Post post) {
    this.id = post.getId();
    this.userNickname = post.getUser().getNickname();
    this.title = post.getTitle();
    this.content = post.getContent();
  }

  private Long id;

  private String userNickname;

  private String title;

  private String content;

}
