package com.personal.board.dto.response.board;

import com.personal.board.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseWithCreatedAt extends BoardResponse {

  public BoardResponseWithCreatedAt(final Board board) {
    super(board);
    this.createdAt = board.getCreatedAt();
  }

  private LocalDateTime createdAt;

}
