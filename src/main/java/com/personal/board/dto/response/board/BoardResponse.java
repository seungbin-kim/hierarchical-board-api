package com.personal.board.dto.response.board;

import com.personal.board.entity.Board;
import lombok.Getter;

@Getter
public class BoardResponse {

  public BoardResponse(Board board) {
    this.id = board.getId();
    this.name = board.getName();
  }

  private Long id;

  private String name;

}
