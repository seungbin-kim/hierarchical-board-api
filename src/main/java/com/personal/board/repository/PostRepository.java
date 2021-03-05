package com.personal.board.repository;

import com.personal.board.entity.Post;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepository {

  @PersistenceContext
  private EntityManager em;

  public Post save(Post post) {
    em.persist(post);
    return post;
  }

  public void updateGroupOrder(Post group, int groupOrder) {
    int updatedRow = em.createQuery(
        "UPDATE Post p SET p.groupOrder = p.groupOrder + 1 WHERE p.group = :group AND p.groupOrder > :groupOrder")
        .setParameter("group", group)
        .setParameter("groupOrder", groupOrder)
        .executeUpdate();
  }

  public Optional<Post> findPostById(Long id) {
    try {
      Post post = em.createQuery(
          "SELECT p FROM Post p WHERE p.id = :id", Post.class)
          .setParameter("id", id)
          .getSingleResult();
      return Optional.of(post);
    } catch (NoResultException exception) {
      return Optional.empty();
    }
  }

  public List<Post> findAllPost() {
    return em.createQuery(
        "SELECT p FROM Post p ORDER BY p.group DESC, p.groupOrder ASC", Post.class)
        .getResultList();
  }

}
