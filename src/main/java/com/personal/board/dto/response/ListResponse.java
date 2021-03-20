package com.personal.board.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ListResponse<T> {

  private final List<T> content;

}
