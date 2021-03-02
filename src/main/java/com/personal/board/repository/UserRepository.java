package com.personal.board.repository;

import com.personal.board.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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

  public Optional<User> findUserById(final Long id) {
    List<User> result = em.createQuery(
        "SELECT u FROM User u WHERE u.id = :id", User.class)
        .setParameter("id", id)
        .getResultList();

    return makeOptionalObject(result);
  }

  public Optional<User> findUserByEmail(final String email) {
    List<User> result = em.createQuery(
        "SELECT u FROM User u WHERE u.email = :email", User.class)
        .setParameter("email", email)
        .getResultList();

    return makeOptionalObject(result);
  }

  public Optional<User> findUserByNickname(final String nickname) {
    List<User> result = em.createQuery(
        "SELECT u FROM User u WHERE u.nickname = :nickname", User.class)
        .setParameter("nickname", nickname)
        .getResultList();

    return makeOptionalObject(result);
  }

  public List<User> findAllUsers() {
    return em.createQuery(
        "SELECT u FROM User u", User.class)
        .getResultList();
  }

  private Optional<User> makeOptionalObject(final List<User> list) {
    try {
      return Optional.of(list.get(0));
    } catch (IndexOutOfBoundsException exception) {
      return Optional.empty();
    }

  }

}
