package com.personal.board.repository;

import com.personal.board.entity.Comment;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class CommentRepository {

  @PersistenceContext
  private EntityManager em;


  public Comment save(final Comment comment) {
    em.persist(comment);
    return comment;
  }


  public Optional<Comment> findCommentByIdAndPostId(final Long commentId, final Long postId) {
    try {
      Comment comment = em.createQuery(
          "SELECT c" +
              " FROM Comment c" +
              " WHERE c.id = :commentId" +
              " AND c.post.id = :postId", Comment.class)
          .setParameter("commentId", commentId)
          .setParameter("postId", postId)
          .getSingleResult();

      return Optional.of(comment);
    } catch (Exception exception) {
      return Optional.empty();
    }
  }


  public void deleteComment(final Comment comment) {
    em.remove(comment);
  }

}
