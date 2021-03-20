package com.personal.board.repository.query;

import com.personal.board.dto.response.post.PostDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PostQueryRepository {

  @PersistenceContext
  EntityManager em;


  public List<PostDto> findAllPostByDto(final Long boardId) {
    List<PostDto> parentList = getParentPostDtos(boardId);

    List<PostDto> parentListForLoop = parentList;
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = parentListForLoop.stream()
          .map(PostDto::getId)
          .collect(Collectors.toList());

      List<PostDto> children = getChildPostDtos(boardId, parentIds);

      Map<Long, List<PostDto>> childrenPostMap = children.stream()
          .collect(Collectors.groupingBy(PostDto::getParentId));

      parentListForLoop.forEach(p -> p.setReply(childrenPostMap.get(p.getId())));

      parentListForLoop = children;
    }

    return parentList;
  }


  private List<PostDto> getChildPostDtos(Long boardId, List<Long> parentIds) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.post.PostDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IN :parentIds" +
            " ORDER BY p.id ASC", PostDto.class)
        .setParameter("boardId", boardId)
        .setParameter("parentIds", parentIds)
        .getResultList();
  }


  private List<PostDto> getParentPostDtos(Long boardId) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.post.PostDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL" +
            " ORDER BY p.id DESC", PostDto.class)
        .setParameter("boardId", boardId)
        .getResultList();
  }

}
