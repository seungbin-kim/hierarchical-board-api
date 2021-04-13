package com.personal.board.repository;

import com.personal.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

  /**
   * 게시판 이름확인
   *
   * @param name 이름
   * @return 이미 이름이 있다면 true, 없다면 false
   */
  boolean existsByName(String name);

}
