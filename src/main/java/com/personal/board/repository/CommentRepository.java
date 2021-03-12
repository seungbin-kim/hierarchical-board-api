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


  public Optional<Comment> findCommentById(final Long id) {
    try {
      Comment comment = em.createQuery(
          "SELECT c FROM Comment c WHERE c.id = :id", Comment.class)
          .setParameter("id", id)
          .getSingleResult();
      return Optional.of(comment);
    } catch (NoResultException exception) {
      return Optional.empty();
    }
  }


  public List<Comment> findAllComment(final Long postId) {
    return em.createQuery(
        "SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.id ASC", Comment.class)
        .setParameter("postId", postId)
        .getResultList();
  }

  public void deleteComment(final Comment findComment) {
    findComment.changeDeletionStatus();
  }

}
