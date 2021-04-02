package com.personal.board.repository;

import com.personal.board.entity.Post;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class PostRepository {

  @PersistenceContext
  private EntityManager em;


  public Post save(final Post post) {
    em.persist(post);
    return post;
  }


  public Optional<Post> findPostByIdAndBoardId(final Long postId, final Long boardId) {
    String query = "SELECT p FROM Post p WHERE p.id = :postId";

    Post post;
    try {
      if (boardId != null) {
        query += " AND p.board.id = :boardId";
        post = em.createQuery(query, Post.class)
            .setParameter("postId", postId)
            .setParameter("boardId", boardId)
            .getSingleResult();
      } else {
        post = em.createQuery(query, Post.class)
            .setParameter("postId", postId)
            .getSingleResult();
      }
      return Optional.of(post);
    } catch (Exception exception) {
      return Optional.empty();
    }
  }


  public void deletePost(final Post post) {
    em.remove(post);
  }


  public void setWriterIdToNull(final Long userId) {
    em.createQuery(
        "UPDATE Post p" +
            " SET p.user = NULL" +
            " WHERE p.user.id = :userId")
        .setParameter("userId", userId)
        .executeUpdate();
  }

}
