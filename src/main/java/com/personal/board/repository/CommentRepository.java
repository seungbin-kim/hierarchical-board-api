package com.personal.board.repository;

import com.personal.board.dto.query.CommentIdAndPostIdQueryDto;
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


  /**
   * 댓글 페이징조회 (원 댓글만 조회)
   * @param postId   게시글 id
   * @param pageable 페이징 정보
   * @return 조회된 댓글(원글) DTO 페이지
   */
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


  /**
   * 부모 댓글의 답 댓글 조회
   * @param postId    게시글 id
   * @param parentIds 부모 댓글 id 목록 (부모글들은 모두 같은계층)
   * @return 부모글들의 답 댓글 목록
   */
  @Query(
      "SELECT new com.personal.board.dto.query.CommentQueryDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
          " FROM Comment c LEFT OUTER JOIN c.user u" +
          " WHERE c.post.id = :postId" +
          " AND c.parent.id IN :parentIds" +
          " ORDER BY c.id ASC")
  List<CommentQueryDto> findAllChildren(@Param("postId") Long postId, @Param("parentIds") List<Long> parentIds);


  /**
   * 게시글들의 댓글 id 조회(각 계층의 게시글들의 댓글수 확인용)
   * @param postIds 게시글 id 목록 (게시글은 모두 같은계층)
   * @return 조회된 목록
   */
  @Query(
      "SELECT new com.personal.board.dto.query.CommentIdAndPostIdQueryDto(c.id, c.post.id)" +
          " FROM Comment c" +
          " WHERE c.post.id IN :postIds")
  List<CommentIdAndPostIdQueryDto> findCommentIdByPostId(@Param("postIds") List<Long> postIds);

}
