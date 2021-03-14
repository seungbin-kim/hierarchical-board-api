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


  public Optional<Board> findBoardById(final Long boardId) {
    Board board = em.find(Board.class, boardId);
    if (board == null) {
      return Optional.empty();
    }
    return Optional.of(board);
  }


  public Optional<Board> findBoardByName(final String name) {
    try {
      Board board = em.createQuery(
          "SELECT b FROM Board b WHERE b.name = :name", Board.class)
          .setParameter("name", name)
          .getSingleResult();
      return Optional.of(board);
    } catch (NoResultException exception) {
      return Optional.empty();
    }
  }


  public List<Board> findAllBoard() {
    return em.createQuery(
        "SELECT b FROM Board b", Board.class)
        .getResultList();
  }

}
