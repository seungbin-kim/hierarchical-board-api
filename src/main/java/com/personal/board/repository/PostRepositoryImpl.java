package com.personal.board.repository;

import com.personal.board.entity.Post;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.Optional;

import static com.personal.board.entity.QPost.*;

public class PostRepositoryImpl implements PostRepositoryCustom {

  private final JPAQueryFactory queryFactory;


  public PostRepositoryImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }


  /**
   * 게시글 id로 게시글찾기 또는 게시판 id와 게시글 id로 게시글 찾기
   * @param postId  게시글 id
   * @param boardId 게시판 id. null 가능
   * @return 조회한 게시글
   */
  @Override
  public Optional<Post> findPostByIdAndBoardId(final Long postId,
                                               final Long boardId) {

    Post findPost = queryFactory
        .selectFrom(post)
        .where(post.id.eq(postId),
            boardIdEq(boardId)
        )
        .fetchFirst();
    return Optional.ofNullable(findPost);
  }


  private Predicate boardIdEq(final Long boardId) {
    return (boardId != null) ? post.board.id.eq(boardId) : null;
  }

}
