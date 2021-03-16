package com.personal.board.repository;

import com.personal.board.entity.Post;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    EntityGraph<?> entityGraph = em.getEntityGraph("Post.user");
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", entityGraph);

    Post post = em.find(Post.class, postId, hints);
    if (post == null) {
      return Optional.empty();
    }
    return Optional.of(post);
  }


  public List<Post> findAllPost(final Long boardId) {
    return em.createQuery(
        "SELECT DISTINCT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.children" +
            " WHERE p.board.id = :boardId ORDER BY p.parent.id ASC, p.id DESC", Post.class)
        .setParameter("boardId", boardId)
        .getResultList();
  }


  public void deletePost(final Post post) {
    em.remove(post);
  }

}
