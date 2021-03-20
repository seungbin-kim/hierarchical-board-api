package com.personal.board.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PageDto<T> {

  private final List<T> content;

  private final int totalOriginalElements;

  private final int size;

  private final int totalPages;

  private final int currentPage;

  private final boolean isFirst;

  private final boolean isLast;

}
