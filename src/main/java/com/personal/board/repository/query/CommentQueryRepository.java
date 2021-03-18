package com.personal.board.repository.query;

import com.personal.board.dto.response.comment.CommentDto;
import com.personal.board.dto.response.post.PostDto;
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

  public List<CommentDto> findAllCommentByDto(final Long postId) {
    List<CommentDto> parentList = getParentCommentDtos(postId);

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

    return parentList;
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

  private List<CommentDto> getParentCommentDtos(Long postId) {
    return em.createQuery(
        "SELECT new com.personal.board.dto.response.comment.CommentDto(c.parent.id, c.id, c.user.nickname, c.content, c.createdAt, c.deleted)" +
            " FROM Comment c JOIN c.user u" +
            " WHERE c.post.id = :postId" +
            " AND c.parent.id IS NULL" +
            " ORDER BY c.id ASC", CommentDto.class)
        .setParameter("postId", postId)
        .getResultList();
  }

}
