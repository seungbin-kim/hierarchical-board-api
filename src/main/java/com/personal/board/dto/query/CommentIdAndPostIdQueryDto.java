package com.personal.board.dto.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentIdAndPostIdQueryDto {

  private final Long commentId;

  private final Long postId;

}
