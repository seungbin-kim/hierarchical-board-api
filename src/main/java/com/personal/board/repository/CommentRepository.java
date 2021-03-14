package com.personal.board.repository;

import com.personal.board.entity.Comment;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepository {

  @PersistenceContext
  private EntityManager em;


  public Comment save(final Comment comment) {
    em.persist(comment);
    return comment;
  }


  public Optional<Comment> findCommentById(final Long boardId) {
    Comment comment = em.find(Comment.class, boardId);
    if (comment == null) {
      return Optional.empty();
    }
    return Optional.of(comment);
  }


  public List<Comment> findAllComment(final Long postId) {
    return em.createQuery(
        "SELECT c FROM Comment c WHERE c.parent IS NULL AND c.post.id = :postId ORDER BY c.id ASC", Comment.class)
        .setParameter("postId", postId)
        .getResultList();
  }

  public void deleteComment(final Comment findComment) {
    findComment.changeDeletionStatus();
  }

}
