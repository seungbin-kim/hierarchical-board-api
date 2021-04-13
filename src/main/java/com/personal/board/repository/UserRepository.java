package com.personal.board.repository;

import com.personal.board.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * email 존재확인
   * @param email 이메일
   * @return 존재시 true, 아니라면 false
   */
  boolean existsByEmail(String email);


  /**
   * 닉네임 존재확인
   * @param nickname 닉네임
   * @return 존재시 true, 아니라면 false
   */
  boolean existsByNickname(String nickname);


  /**
   * 유저 정보와 권한정보 같이조회
   * @param email 이메일
   * @return 조회된 유저
   */
  @Query("SELECT u FROM User u JOIN FETCH u.authorities WHERE u.email = :email")
  Optional<User> findByEmailWithAuthorities(@Param("email") String email);


  /**
   * 유저목록 페이징조회
   * @param pageable 페이징 정보
   * @return 페이징된 유저목록
   */
  @NonNull
  Page<User> findAll(@NonNull Pageable pageable);

}
