package com.personal.board.dto.response;

import com.personal.board.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseWithDate extends BoardResponse {

  public BoardResponseWithDate(Board board) {
    super(board);
    this.createdAt = board.getCreatedAt();
    this.modifiedAt = board.getModifiedAt();
  }

  private final LocalDateTime createdAt;

  private final LocalDateTime modifiedAt;

}
