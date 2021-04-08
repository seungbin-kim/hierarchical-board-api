package com.personal.board.repository.query;

import com.personal.board.dto.query.CommentIdAndPostIdQueryDto;
import com.personal.board.dto.query.CommentQueryDto;
import com.personal.board.dto.response.PageQueryDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CommentQueryJpaRepository {

  @PersistenceContext
  EntityManager em;


  public PageQueryDto<CommentQueryDto> findPageableCommentByDto(final Long postId, final int size, final int page) {
    List<CommentQueryDto> parentList = getParentCommentDtos(postId, size, page);

    int totalParentCommentCount = getParentCommentCount(postId);
    int totalPages = (totalParentCommentCount / size) - 1;
    if (totalParentCommentCount % size != 0) {
      totalPages++;
    }
    boolean isFirst = (page == 0);
    boolean isLast = (page == totalPages);

    List<CommentQueryDto> parentListForLoop = parentList;
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = parentListForLoop.stream()
          .map(CommentQueryDto::getId)
          .collect(Collectors.toList());

      List<CommentQueryDto> children = getChildCommentDtos(postId, parentIds);

      Map<Long, List<CommentQueryDto>> childrenCommentMap = children.stream()
          .collect(Collectors.groupingBy(CommentQueryDto::getParentId));

      parentListForLoop.forEach(c -> c.setReply(childrenCommentMap.get(c.getId())));

      parentListForLoop = children;
    }

    return new PageQueryDto<>(parentList, totalParentCommentCount, size, totalPages, page, isFirst, isLast);
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


  private List<CommentQueryDto> getChildCommentDtos(Long postId, List<Long> parentIds) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.query.CommentQueryDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
            " FROM Comment c LEFT OUTER JOIN c.user u" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IN :parentIds" +
            " ORDER BY c.id ASC", CommentQueryDto.class)
        .setParameter("postId", postId)
        .setParameter("parentIds", parentIds)
        .getResultList();
  }


  private List<CommentQueryDto> getParentCommentDtos(final Long postId, final int size, final int page) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.query.CommentQueryDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
            " FROM Comment c LEFT OUTER JOIN c.user u" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IS NULL" +
            " ORDER BY c.id ASC", CommentQueryDto.class)
        .setParameter("postId", postId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }


  public List<CommentIdAndPostIdQueryDto> findCommentIdByPostId(final List<Long> postIds) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.query.CommentIdAndPostIdQueryDto(c.id, c.post.id)" +
            " FROM Comment c" +
            " WHERE c.post.id IN :postIds", CommentIdAndPostIdQueryDto.class)
        .setParameter("postIds", postIds)
        .getResultList();
  }

}
