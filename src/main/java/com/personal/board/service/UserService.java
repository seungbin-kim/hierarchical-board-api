package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.UserResponseWithCreatedAt;
import com.personal.board.dto.response.UserResponseWithDate;
import com.personal.board.dto.response.UserResponseWithModifiedAt;
import com.personal.board.entity.User;
import com.personal.board.exception.*;
import com.personal.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserResponseWithCreatedAt signUp(final SignUpRequest request) {
    if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
      throw new EmailDuplicatedException();
    } else if (userRepository.findUserByNickname(request.getNickname()).isPresent()) {
      throw new NicknameDuplicatedException();
    }

    User user = new User(
        request.getEmail(),
        request.getNickname(),
        request.getName(),
        request.getAge(),
        request.getPassword());

    User savedUser = userRepository.save(user);

    return new UserResponseWithCreatedAt(savedUser);
  }

  public void deleteUser(final Long id) {
    Optional<User> userById = userRepository.findUserById(id);
    if (userById.isEmpty()) {
      throw new NotFoundException("user id not found.");
    }

    userRepository.deleteUser(userById.get());
  }

  public UserResponseWithDate findUser(final Long id) {
    Optional<User> userById = userRepository.findUserById(id);
    if (userById.isEmpty()) {
      throw new NotFoundException("user id not found.");
    }

    return new UserResponseWithDate(userById.get());
  }

  public List<UserResponseWithDate> getAllUsers() {
    return userRepository.findAllUsers()
        .stream()
        .map(UserResponseWithDate::new)
        .collect(Collectors.toList());
  }

  public UserResponseWithModifiedAt updateUser(final UserUpdateRequest request, final Long id) throws IllegalAccessException {
    // 정보 찾아오기
    Optional<User> userById = userRepository.findUserById(id);
    if (userById.isEmpty()) {
      throw new NotFoundException("user id not found.");
    }
    User findUser = userById.get();

    // Password 체크
    if (!findUser.getPassword().equals(request.getPassword())) {
      throw new PasswordIncorrectException();
    }

    // 들어온 값 확인
    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = new ArrayList<>();
    for (Field declaredField : declaredFields) {
      declaredField.setAccessible(true);
      String fieldName = declaredField.getName();
      Object fieldValue = declaredField.get(request);
      if (fieldValue == null) {
        continue;
      }
      validatedFields.add(fieldName);
    }

    // 변경감지
    for (String validatedField : validatedFields) {
      switch (validatedField) {
        case "email":
          if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new EmailDuplicatedException();
          }
          findUser.changeEmail(request.getEmail());
          break;
        case "nickname":
          if (userRepository.findUserByNickname(request.getEmail()).isPresent()) {
            throw new NicknameDuplicatedException();
          }
          findUser.changeNickname(request.getNickname());
          break;
        case "name":
          findUser.changeName(request.getName());
          break;
        case "age":
          findUser.changeAge(request.getAge());
          break;
        case "newPassword":
          findUser.changePassword(request.getNewPassword());
          break;
      }
    }
    findUser.setModifiedAt(LocalDateTime.now());

    return new UserResponseWithModifiedAt(findUser);
  }

}
