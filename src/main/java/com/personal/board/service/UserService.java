package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.response.UserResponse;
import com.personal.board.dto.response.UserResponseWithCreatedAt;
import com.personal.board.dto.response.UserResponseWithModifiedAt;
import com.personal.board.entity.User;
import com.personal.board.exception.DuplicatedException;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserResponseWithCreatedAt signUp(SignUpRequest signUpRequest) {
    if (userRepository.findUserByEmailOrNickname(signUpRequest.getEmail(), signUpRequest.getNickname()) != null) {
      throw new DuplicatedException("Email or nickname is duplicated.");
    }

    User user = new User(
        signUpRequest.getEmail(),
        signUpRequest.getNickname(),
        signUpRequest.getName(),
        signUpRequest.getAge(),
        signUpRequest.getPassword());

    User savedUser = userRepository.save(user);

    return new UserResponseWithCreatedAt(savedUser);
  }

  public List<UserResponseWithModifiedAt> returnAllUsers() {
    return userRepository.findAllUsers()
        .stream()
        .map(UserResponseWithModifiedAt::new)
        .collect(Collectors.toList());
  }

}
