package com.personal.board.repository;

import com.personal.board.entity.Post;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepository {

  @PersistenceContext
  private EntityManager em;


  public Post save(final Post post) {
    em.persist(post);
    return post;
  }


  public Optional<Post> findPostById(final Long postId) {
    Post post = em.find(Post.class, postId);
    if (post == null) {
      return Optional.empty();
    }
    return Optional.of(post);
  }


  public List<Post> findAllPost(final Long boardId) {
    return em.createQuery(
        "SELECT p FROM Post p WHERE p.parent IS NULL AND p.board.id = :boardId ORDER BY p.id DESC", Post.class)
        .setParameter("boardId", boardId)
        .getResultList();
  }


  public void deletePost(final Post post) {
    em.remove(post);
  }

}
