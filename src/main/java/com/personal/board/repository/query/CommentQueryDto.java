package com.personal.board.repository.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommentQueryDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Long parentId;

  private final Long id;

  private final String userNickname;

  private final String content;

  private final LocalDateTime createdAt;

  private final boolean deletedStatus;

  private List<CommentQueryDto> reply;

  public void setReply(List<CommentQueryDto> reply) {
    if (reply == null) {
      this.reply = new ArrayList<>();
      return;
    }
    this.reply = reply;
  }

}
