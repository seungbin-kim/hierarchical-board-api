package com.personal.board.dto.response.board;

import com.personal.board.entity.Board;
import lombok.Getter;

@Getter
public class BoardResponse {

  private Long id;

  private String name;

  public BoardResponse(final Board board) {
    this.id = board.getId();
    this.name = board.getName();
  }

}
