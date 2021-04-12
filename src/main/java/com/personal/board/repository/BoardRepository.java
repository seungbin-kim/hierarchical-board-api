package com.personal.board.repository;

import com.personal.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

  boolean existsByName(String name);

}
