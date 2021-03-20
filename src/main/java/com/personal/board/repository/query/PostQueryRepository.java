package com.personal.board.repository.query;

import com.personal.board.dto.response.PageDto;
import com.personal.board.dto.response.ListResponse;
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


  public PageDto<PostDto> findPageablePostByDto(final Long boardId, final int size, final int page) {
    List<PostDto> parentList = getParentPostDtos(boardId, size, page);

    int totalParentPostCount = getParentPostCount(boardId);
    int totalPages = (totalParentPostCount / size) - 1;
    if (totalParentPostCount % size != 0) {
      totalPages++;
    }
    boolean isFirst = (page == 0);
    boolean isLast = (page == totalPages);

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

    return new PageDto<>(parentList, totalParentPostCount, size, totalPages, page, isFirst, isLast);
  }

  private int getParentPostCount(Long boardId) {
    return em.createQuery(
        "SELECT COUNT(p)" +
            " FROM Post p" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL", Long.class)
        .setParameter("boardId", boardId)
        .getSingleResult()
        .intValue();
  }


  private List<PostDto> getChildPostDtos(final Long boardId, final List<Long> parentIds) {
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


  private List<PostDto> getParentPostDtos(final Long boardId, final int size, final int page) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.post.PostDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL" +
            " ORDER BY p.id DESC", PostDto.class)
        .setParameter("boardId", boardId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

}
