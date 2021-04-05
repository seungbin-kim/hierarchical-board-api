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

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  @Query("SELECT u FROM User u JOIN FETCH u.authorities WHERE u.email = :email")
  Optional<User> findByEmailWithAuthorities(@Param("email") String email);

  @NonNull
  Page<User> findAll(@NonNull Pageable pageable);

}
