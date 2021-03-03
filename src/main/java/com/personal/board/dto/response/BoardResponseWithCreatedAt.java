package com.personal.board.dto.response;

import com.personal.board.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseWithCreatedAt extends BoardResponse {

  public BoardResponseWithCreatedAt(Board board) {
    super(board);
    this.createdAt = board.getCreatedAt();
  }

  private LocalDateTime createdAt;

}
