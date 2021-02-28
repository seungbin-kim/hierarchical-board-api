package com.personal.board.repository;

import com.personal.board.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class UserRepository {

  @PersistenceContext
  private EntityManager em;

  public User save(User user) {
    em.persist(user);
    return user;
  }

  public User findUserByEmailOrNickname(String email, String nickname) {
    List<User> resultList = em.createQuery(
        "SELECT u FROM User u WHERE u.email = :email OR u.nickname = :nickname", User.class)
        .setParameter("email", email)
        .setParameter("nickname", nickname)
        .getResultList();

    return (resultList.size() == 0) ? null : resultList.get(0);
  }

  public List<User> findAllUsers() {
    return em.createQuery(
        "SELECT u FROM User u", User.class)
        .getResultList();
  }

}
