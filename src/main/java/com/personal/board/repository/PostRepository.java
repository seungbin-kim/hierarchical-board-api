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

  @Modifying
  @Query("UPDATE Post p SET p.user = NULL WHERE p.user.id = :userId")
  void updateWriterIdToNull(@Param("userId") Long userId);

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

  @Query(
      "SELECT new com.personal.board.dto.query.PostQueryDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
          " FROM Post p LEFT OUTER JOIN p.user u" +
          " WHERE p.board.id = :boardId" +
          " AND p.parent.id IN :parentIds" +
          " ORDER BY p.id ASC")
  List<PostQueryDto> findAllChildren(@Param("boardId") Long boardId, @Param("parentIds") List<Long> parentIds, Pageable pageable);

}
