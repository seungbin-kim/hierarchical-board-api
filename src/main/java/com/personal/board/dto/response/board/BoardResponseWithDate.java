package com.personal.board.dto.response.board;

import com.personal.board.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseWithDate extends BoardResponse {

  private final LocalDateTime createdAt;

  private final LocalDateTime modifiedAt;

  public BoardResponseWithDate(final Board board) {
    super(board);
    this.createdAt = board.getCreatedAt();
    this.modifiedAt = board.getModifiedAt();
  }

}
