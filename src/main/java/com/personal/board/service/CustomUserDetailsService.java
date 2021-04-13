package com.personal.board.service;

import com.personal.board.entity.User;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * 로그인시 동작
   *
   * @param email 유저 이메일
   * @return UserDetails
   * @throws UsernameNotFoundException
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {

    return userRepository.findByEmailWithAuthorities(email)
        .map(this::createUser)
        .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));
  }

  /**
   * UserDetails 생성
   *
   * @param user 유저 엔티티
   * @return UserDetails
   */
  private org.springframework.security.core.userdetails.User createUser(final User user) {

    List<SimpleGrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
        .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().toString()))
        .collect(Collectors.toList());
    return new org.springframework.security.core.userdetails.User(
        String.valueOf(user.getId()), user.getPassword(), grantedAuthorities);
  }

}
