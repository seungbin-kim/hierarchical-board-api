package com.personal.board.repository;

import com.personal.board.entity.Post;

import java.util.Optional;

public interface PostRepositoryCustom {

  Optional<Post> findPostByIdAndBoardId(final Long postId, final Long boardId);

}
