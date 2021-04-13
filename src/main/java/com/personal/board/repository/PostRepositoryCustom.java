package com.personal.board.repository;

import com.personal.board.entity.Post;

import java.util.Optional;

/**
 * Querydsl 를 쓰기 위한 커스텀 레포지토리 인터페이스
 */
public interface PostRepositoryCustom {

  Optional<Post> findPostByIdAndBoardId(Long postId, Long boardId);

}
