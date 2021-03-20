package com.personal.board.repository;

import com.personal.board.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

  @PersistenceContext
  private EntityManager em;


  public User save(final User user) {
    em.persist(user);
    return user;
  }


  public void deleteUser(final User user) {
    em.remove(user);
  }


  public Optional<User> findUserById(final Long userId) {
    User user = em.find(User.class, userId);
    if (user == null) {
      return Optional.empty();
    }
    return Optional.of(user);
  }


  public boolean checkUserEmail(final String email) {
    Long result = em.createQuery(
        "SELECT count(u) FROM User u WHERE u.email = :email", Long.class)
        .setParameter("email", email)
        .getSingleResult();
    return result == 1;
  }


  public boolean checkUserNickname(final String nickname) {
    Long result = em.createQuery(
        "SELECT count(u) FROM User u WHERE u.nickname = :nickname", Long.class)
        .setParameter("nickname", nickname)
        .getSingleResult();
    return result == 1;
  }


  public List<User> findPageableUsers(final int size, final int page) {
    return em.createQuery(
        "SELECT u FROM User u", User.class)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }


  public int getUserCount() {
    return em.createQuery(
        "SELECT COUNT(u) FROM User u", Long.class)
        .getSingleResult()
        .intValue();
  }

}