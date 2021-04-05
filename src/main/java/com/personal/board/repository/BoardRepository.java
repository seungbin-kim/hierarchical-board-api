package com.personal.board.repository;

import com.personal.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

  Optional<Board> findByName(String name);

}
