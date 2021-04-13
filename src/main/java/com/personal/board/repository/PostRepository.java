package com.personal.board.repository;

import com.personal.board.entity.Post;
import com.personal.board.dto.query.PostQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  /**
   * 유저 탈퇴시 게시글과의 참조관계를 지우기위한 업데이트 쿼리
   * @param userId 유저 id
   */
  @Modifying
  @Query("UPDATE Post p SET p.user = NULL WHERE p.user.id = :userId")
  void updateWriterIdToNull(@Param("userId") Long userId);


  /**
   * 게시글 페이징조회(원글만 조회)
   * @param boardId  게시판 id
   * @param pageable 페이징 정보
   * @return 조회된 게시글(원글) DTO 페이지
   */
  @Query(
      value =
          "SELECT new com.personal.board.dto.query.PostQueryDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
              " FROM Post p LEFT OUTER JOIN p.user u" +
              " WHERE p.board.id = :boardId" +
              " AND p.parent.id IS NULL" +
              " ORDER BY p.id DESC",
      countQuery =
          "SELECT COUNT(p)" +
              " FROM Post p" +
              " WHERE p.board.id = :boardId" +
              " AND p.parent.id IS NULL")
  Page<PostQueryDto> findAllOriginal(@Param("boardId") Long boardId, Pageable pageable);


  /**
   * 부모글의 답글 조회
   * @param boardId   게시판 id
   * @param parentIds 부모글의 id 목록 (부모글들은 모두 같은계층)
   * @return 부모글들의 답글 목록
   */
  @Query(
      "SELECT new com.personal.board.dto.query.PostQueryDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
          " FROM Post p LEFT OUTER JOIN p.user u" +
          " WHERE p.board.id = :boardId" +
          " AND p.parent.id IN :parentIds" +
          " ORDER BY p.id ASC")
  List<PostQueryDto> findAllChildren(@Param("boardId") Long boardId, @Param("parentIds") List<Long> parentIds);

}
