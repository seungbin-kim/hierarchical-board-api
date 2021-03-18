package com.personal.board.dto.response.post;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ParentPostDto {

  private final Long id;

  private final String title;

  private final String userNickname;

  private final boolean deletedStatus;

  private final LocalDateTime createdAt;

  private List<ChildPostDto> reply;

}
