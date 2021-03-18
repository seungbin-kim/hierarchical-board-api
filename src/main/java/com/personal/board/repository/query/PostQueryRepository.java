package com.personal.board.repository.query;

import com.personal.board.dto.response.post.ChildPostDto;
import com.personal.board.dto.response.post.ParentPostDto;
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


  public List<ParentPostDto> findAllPostByDto(final Long boardId) {
    List<ParentPostDto> parentList = getParentPostDtos(boardId);

    List<Long> parentIds = parentList.stream()
        .map(ParentPostDto::getId)
        .collect(Collectors.toList());

    List<ChildPostDto> children = getChildPostDtos(boardId, parentIds);

    Map<Long, List<ChildPostDto>> childrenPostMap = children.stream()
        .collect(Collectors.groupingBy(ChildPostDto::getParentId));

    parentList.forEach(p -> p.setReply(childrenPostMap.get(p.getId())));

    List<Long> Ids = children.stream()
        .map(ChildPostDto::getId)
        .collect(Collectors.toList());

    List<ChildPostDto> parentList2 = children;
    while (!Ids.isEmpty()) {
      List<ChildPostDto> children2 = getChildPostDtos(boardId, Ids);

      Map<Long, List<ChildPostDto>> childrenPostMap2 = children2.stream()
          .collect(Collectors.groupingBy(ChildPostDto::getParentId));

      parentList2.forEach(c -> c.setReply(childrenPostMap2.get(c.getId())));

      Ids = children2.stream()
          .map(ChildPostDto::getId)
          .collect(Collectors.toList());

      parentList2 = children2;
    }

    return parentList;
  }


  private List<ChildPostDto> getChildPostDtos(Long boardId, List<Long> parentIds) {
    List<ChildPostDto> children = em.createQuery(
        "SELECT new com.personal.board.dto.response.post.ChildPostDto(p.parent.id, p.id, p.title, u.nickname, p.deleted, p.createdAt)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IN :parentIds" +
            " ORDER BY p.id ASC", ChildPostDto.class)
        .setParameter("boardId", boardId)
        .setParameter("parentIds", parentIds)
        .getResultList();
    return children;
  }


  private List<ParentPostDto> getParentPostDtos(Long boardId) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.post.ParentPostDto(p.id, p.title, u.nickname, p.deleted, p.createdAt)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL" +
            " ORDER BY p.id DESC", ParentPostDto.class)
        .setParameter("boardId", boardId)
        .getResultList();
  }

}
