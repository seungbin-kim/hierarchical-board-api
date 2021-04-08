package com.personal.board.repository;

import com.personal.board.entity.Comment;
import com.personal.board.dto.query.CommentQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Modifying
  @Query("UPDATE Comment c SET c.user = NULL WHERE c.user.id = :userId")
  void updateWriterIdToNull(@Param("userId") Long userId);

  Optional<Comment> findByIdAndPostId(Long commentId, Long PostId);

  @Query(
      value =
          "SELECT new com.personal.board.dto.query.CommentQueryDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
              " FROM Comment c LEFT OUTER JOIN c.user u" +
              " WHERE c.post.id = :postId" +
              " AND c.parent.id IS NULL" +
              " ORDER BY c.id ASC",
      countQuery =
          "SELECT COUNT(c)" +
              "FROM Comment c" +
              " WHERE c.post.id = :postId" +
              " AND c.parent.id IS NULL")
  Page<CommentQueryDto> findAllOriginal(@Param("postId") Long postId, Pageable pageable);

  @Query(
      "SELECT new com.personal.board.dto.query.CommentQueryDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
          " FROM Comment c LEFT OUTER JOIN c.user u" +
          " WHERE c.post.id = :postId" +
          " AND c.parent.id IN :parentIds" +
          " ORDER BY c.id ASC")
  List<CommentQueryDto> findAllChildren(@Param("postId") Long postId, @Param("parentIds") List<Long> parentIds);

}
