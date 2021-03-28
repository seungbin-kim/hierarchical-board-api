package com.personal.board.repository.query;

import com.personal.board.dto.response.PageQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

  @PersistenceContext
  EntityManager em;

  private final CommentQueryRepository commentQueryRepository;


  public PageQueryDto<PostQueryDto> findPageablePostByDto(final Long boardId, final int size, final int page) {
    List<PostQueryDto> parentList = getParentPostDtos(boardId, size, page);

    int totalParentPostCount = getParentPostCount(boardId);
    int totalPages = (totalParentPostCount / size) - 1;
    if (totalParentPostCount % size != 0) {
      totalPages++;
    }
    boolean isFirst = (page == 0);
    boolean isLast = (page == totalPages);

    List<PostQueryDto> parentListForLoop = parentList;
    while (!parentListForLoop.isEmpty()) {
      List<Long> parentIds = parentListForLoop.stream()
          .map(PostQueryDto::getId)
          .collect(Collectors.toList());

      List<PostQueryDto> children = getChildPostDtos(boardId, parentIds);
      Map<Long, List<PostQueryDto>> childrenPostMap = children.stream()
          .collect(Collectors.groupingBy(PostQueryDto::getParentId));

      List<CommentIdAndPostIdQueryDto> commentCountByPostId = commentQueryRepository.findCommentIdByPostId(parentIds);
      Map<Long, List<CommentIdAndPostIdQueryDto>> commentIdMap = commentCountByPostId.stream()
          .collect(Collectors.groupingBy(CommentIdAndPostIdQueryDto::getPostId));

      parentListForLoop.forEach(p -> {
        p.setReply(childrenPostMap.get(p.getId()));
        Optional<List<CommentIdAndPostIdQueryDto>> optionalComments = Optional.ofNullable(commentIdMap.get(p.getId()));
        optionalComments.ifPresent(c -> p.setCommentCount(c.size()));
      });

      parentListForLoop = children;
    }

    return new PageQueryDto<>(parentList, totalParentPostCount, size, totalPages, page, isFirst, isLast);
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


  private List<PostQueryDto> getChildPostDtos(final Long boardId, final List<Long> parentIds) {
    return em.createQuery(
        "SELECT new com.personal.board.repository.query.PostQueryDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IN :parentIds" +
            " ORDER BY p.id ASC", PostQueryDto.class)
        .setParameter("boardId", boardId)
        .setParameter("parentIds", parentIds)
        .getResultList();
  }


  private List<PostQueryDto> getParentPostDtos(final Long boardId, final int size, final int page) {
    return em.createQuery(
        "SELECT new com.personal.board.repository.query.PostQueryDto(p.parent.id, p.id, p.title, u.nickname, p.createdAt, p.deleted)" +
            " FROM Post p JOIN p.user u" +
            " WHERE p.board.id = :boardId" +
            " AND p.parent.id IS NULL" +
            " ORDER BY p.id DESC", PostQueryDto.class)
        .setParameter("boardId", boardId)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

}
