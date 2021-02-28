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

  public User save(User user) {
    em.persist(user);
    return user;
  }

  public Optional<User> findUserById(String id) {
    List<User> result = em.createQuery(
        "SELECT u FROM User u WHERE u.id = :id", User.class)
        .setParameter("id", id)
        .getResultList();

    return makeOptionalObject(result);
  }

  public Optional<User> findUserByEmailOrNickname(String email, String nickname) {
    List<User> result = em.createQuery(
        "SELECT u FROM User u WHERE u.email = :email OR u.nickname = :nickname", User.class)
        .setParameter("email", email)
        .setParameter("nickname", nickname)
        .getResultList();

    return makeOptionalObject(result);
  }

  public List<User> findAllUsers() {
    return em.createQuery(
        "SELECT u FROM User u", User.class)
        .getResultList();
  }

  private Optional<User> makeOptionalObject(List<User> list) {
    try {
      return Optional.of(list.get(0));
    } catch (IndexOutOfBoundsException exception) {
      return Optional.empty();
    }

  }

}
