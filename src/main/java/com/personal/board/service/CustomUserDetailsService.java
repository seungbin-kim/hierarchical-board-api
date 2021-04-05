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

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmailWithAuthorities(email)
        .map(this::createUser)
        .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));
  }

  private org.springframework.security.core.userdetails.User createUser(User user) {
    List<SimpleGrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
        .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName().toString()))
        .collect(Collectors.toList());
    return
        new org.springframework.security.core.userdetails.User(
            String.valueOf(user.getId()), user.getPassword(), grantedAuthorities);
  }

}
