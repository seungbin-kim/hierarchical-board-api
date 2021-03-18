package com.personal.board.dto.response.post;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({"parentId", "id", "title", "userNickname", "deletedStatus", "createdAt"})
public class ChildPostDto extends ParentPostDto {

  private final Long parentId;

  public ChildPostDto(Long parentId, Long id, String title, String userNickname, boolean deletedStatus, LocalDateTime createdAt) {
    super(id, title, userNickname, deletedStatus, createdAt);
    this.parentId = parentId;
  }

}
