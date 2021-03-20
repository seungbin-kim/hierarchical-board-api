package com.personal.board.repository.query;

import com.personal.board.dto.response.PageDto;
import com.personal.board.dto.response.comment.CommentDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CommentQueryRepository {

  @PersistenceContext
  EntityManager em;


  public PageDto<CommentDto> findPageableCommentByDto(final Long postId, final int size, final int page) {
    List<CommentDto> parentList = getParentCommentDtos(postId, size, page);

    int totalParentCommentCount = getParentCommentCount(postId);
    int totalPages = (totalParentCommentCount / size) - 1;
    if (totalParentCommentCount % size != 0) {
      totalPages++;
    }
    boolean isFirst = (page == 0);
    boolean isLast = (page == totalPages);

    List<CommentDto> parentListForLoop = parentList;
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = parentListForLoop.stream()
          .map(CommentDto::getId)
          .collect(Collectors.toList());

      List<CommentDto> children = getChildCommentDtos(postId, parentIds);

      Map<Long, List<CommentDto>> childrenCommentMap = children.stream()
          .collect(Collectors.groupingBy(CommentDto::getParentId));

      parentListForLoop.forEach(c -> c.setReply(childrenCommentMap.get(c.getId())));

      parentListForLoop = children;
    }

    return new PageDto<>(parentList, totalParentCommentCount, size, totalPages, page, isFirst, isLast);
  }


  private int getParentCommentCount(final Long postId) {
    return em.createQuery(
        "SELECT COUNT(c)" +
            " FROM Comment c" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IS NULL", Long.class)
        .setParameter("postId", postId)
        .getSingleResult()
        .intValue();
  }


  private List<CommentDto> getChildCommentDtos(Long postId, List<Long> parentIds) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.comment.CommentDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
            " FROM Comment c JOIN c.user u" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IN :parentIds" +
            " ORDER BY c.id ASC", CommentDto.class)
        .setParameter("postId", postId)
        .setParameter("parentIds", parentIds)
        .getResultList();
  }


  private List<CommentDto> getParentCommentDtos(final Long postId, final int size, final int page) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.comment.CommentDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
            " FROM Comment c JOIN c.user u" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IS NULL" +
            " ORDER BY c.id ASC", CommentDto.class)
        .setParameter("postId", postId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

}
