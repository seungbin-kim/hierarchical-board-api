package com.personal.board.repository.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentIdAndPostIdQueryDto {

  private final Long commentId;

  private final Long postId;

}
