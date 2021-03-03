package com.personal.board.repository;

import com.personal.board.entity.Board;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepository {

  @PersistenceContext
  private EntityManager em;

  public Board save(final Board board) {
    em.persist(board);
    return board;
  }

  public Optional<Board> findBoardById(final Long id) {
    try {
      Board board = em.createQuery(
          "SELECT b FROM Board b WHERE b.id = :id", Board.class)
          .setParameter("id", id)
          .getSingleResult();
      return Optional.of(board);
    } catch (NoResultException exception) {
      return Optional.empty();
    }
  }

  public boolean checkBoardName(final String name) {
    Long result = em.createQuery(
        "SELECT count(b) FROM Board b WHERE b.name = :name", Long.class)
        .setParameter("name", name)
        .getSingleResult();
    return result == 1;
  }

  public List<Board> findAllBoard() {
    return em.createQuery(
        "SELECT b FROM Board b", Board.class)
        .getResultList();
  }

}
