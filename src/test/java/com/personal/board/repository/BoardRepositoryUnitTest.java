package com.personal.board.repository;

import com.personal.board.entity.Board;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureTestDatabase
@DataJpaTest
class BoardRepositoryUnitTest {

  @Autowired
  BoardRepository boardRepository;

  @Test
  @DisplayName("게시판이름확인")
  void existsByName() throws Exception {
    //given
    String boardName1 = "test1";
    String boardName2 = "test2";
    Board board = new Board(boardName1);
    boardRepository.save(board);

    //when
    boolean testResult1 = boardRepository.existsByName(boardName1);
    boolean testResult2 = boardRepository.existsByName(boardName2);

    //then
    assertThat(testResult1).isTrue();
    assertThat(testResult2).isFalse();
  }

}