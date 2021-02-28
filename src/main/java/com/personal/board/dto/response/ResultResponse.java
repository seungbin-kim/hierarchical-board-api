package com.personal.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ResultResponse<T> {

  private T content;

}
