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

    List<Long> parentIds = parentList.stream()
        .map(PostDto::getId)
        .collect(Collectors.toList());

    List<PostDto> children = getChildPostDtos(boardId, parentIds);

    Map<Long, List<PostDto>> childrenPostMap = children.stream()
        .collect(Collectors.groupingBy(PostDto::getParentId));

    parentList.forEach(p -> p.setReply(childrenPostMap.get(p.getId())));

    List<Long> Ids = children.stream()
        .map(PostDto::getId)
        .collect(Collectors.toList());

    List<PostDto> parentList2 = children;
    while (!Ids.isEmpty()) {
      List<PostDto> children2 = getChildPostDtos(boardId, Ids);

      Map<Long, List<PostDto>> childrenPostMap2 = children2.stream()
          .collect(Collectors.groupingBy(PostDto::getParentId));

      parentList2.forEach(c -> c.setReply(childrenPostMap2.get(c.getId())));

      Ids = children2.stream()
          .map(PostDto::getId)
          .collect(Collectors.toList());

      parentList2 = children2;
    }

    return parentList;
  }


  private List<PostDto> getChildPostDtos(Long boardId, List<Long> parentIds) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.post.PostDto(p.parent.id, p.id, p.title, u.nickname, p.deleted, p.createdAt)" +
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
        "SELECT new com.personal.board.dto.response.post.PostDto(p.parent.id, p.id, p.title, u.nickname, p.deleted, p.createdAt)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL" +
            " ORDER BY p.id DESC", PostDto.class)
        .setParameter("boardId", boardId)
        .getResultList();
  }

}
