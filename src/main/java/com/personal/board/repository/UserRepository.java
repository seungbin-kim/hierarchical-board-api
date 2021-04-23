package com.personal.board.repository;

import com.personal.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * 유저 탈퇴시 게시글과의 참조관계를 지우기위한 업데이트 쿼리
   *
   * @param userId 유저 id
   */
  @Modifying
  @Query("UPDATE Post p SET p.user = NULL WHERE p.user.id = :userId")
  void updateWriterIdToNull(@Param("userId") Long userId);

  /**
   * email 존재확인
   *
   * @param email 이메일
   * @return 존재시 true, 아니라면 false
   */
  boolean existsByEmail(String email);


  /**
   * 닉네임 존재확인
   *
   * @param nickname 닉네임
   * @return 존재시 true, 아니라면 false
   */
  boolean existsByNickname(String nickname);


  /**
   * 유저 정보와 권한정보 같이조회
   *
   * @param email 이메일
   * @return 조회된 유저
   */
  @Query("SELECT u FROM User u JOIN FETCH u.authorities WHERE u.email = :email")
  Optional<User> findByEmailWithAuthorities(@Param("email") String email);

}
