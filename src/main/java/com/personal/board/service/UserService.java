package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.PageQueryDto;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
import com.personal.board.entity.Authority;
import com.personal.board.entity.User;
import com.personal.board.enumeration.Role;
import com.personal.board.exception.*;
import com.personal.board.repository.CommentRepository;
import com.personal.board.repository.PostRepository;
import com.personal.board.repository.UserRepository;
import com.personal.board.util.PatchUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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

  private final CommentRepository commentRepository;

  private final PostRepository postRepository;

  private final PasswordEncoder passwordEncoder;


  public UserResponseWithCreatedAt signUp(final SignUpRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new EmailDuplicatedException();
    }
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new NicknameDuplicatedException();
    }

    Authority authority = new Authority(Role.ROLE_USER);

    User user = User.createUser(
        request.getEmail(),
        request.getNickname(),
        request.getName(),
        request.getBirthday(),
        passwordEncoder.encode(request.getPassword()),
        authority);

    User savedUser = userRepository.save(user);
    return new UserResponseWithCreatedAt(savedUser);
  }


  public void deleteUser(final Long userId) {
    Optional<User> userById = userRepository.findById(userId);
    if (userById.isEmpty()) {
      throw new UserNotFoundException();
    }
    commentRepository.setWriterIdToNull(userId);
    postRepository.setWriterIdToNull(userId);
    userRepository.delete(userById.get());
  }


  @Transactional(readOnly = true)
  public UserResponseWithDate getUser(final Long userId) {
    Optional<User> userById = userRepository.findById(userId);
    if (userById.isEmpty()) {
      throw new UserNotFoundException();
    }
    return new UserResponseWithDate(userById.get());
  }


//  @Transactional(readOnly = true)
//  public PageQueryDto<UserResponseWithDate> getPageableUsers(final long size, final long page) {
//    List<UserResponseWithDate> result = userRepository.findPageableUsers(size, page)
//        .stream()
//        .map(UserResponseWithDate::new)
//        .collect(Collectors.toList());
//
//    long totalUserCount = userRepository.count();
//    long totalPages = (totalUserCount / size) - 1;
//    if (totalUserCount % size != 0) {
//      totalPages++;
//    }
//    boolean isFirst = (page == 0);
//    boolean isLast = (page == totalPages);
//
//    return new PageQueryDto<>(result, totalUserCount, size, totalPages, page, isFirst, isLast);
//  }


  @Transactional(readOnly = true)
  public Page<UserResponseWithDate> getPageableUsers(final Pageable pageable) {
    Page<User> userPage = userRepository.findAll(pageable);
    return userPage.map(UserResponseWithDate::new);
  }


  public UserResponseWithModifiedAt updateUser(
      final UserUpdateRequest request,
      final Long userId) throws IllegalAccessException {
    // 유저정보 찾아오기
    Optional<User> userById = userRepository.findById(userId);
    userById.orElseThrow(UserNotFoundException::new);
    User findUser = userById.get();

    // Password 체크
    if (!passwordEncoder.matches(request.getPassword(), findUser.getPassword())) {
      throw new PasswordIncorrectException();
    }

    // 들어온 값 확인
    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = PatchUtil.validateFields(request, declaredFields); // 입력필드들 얻기

    // 변경감지
    for (String validatedField : validatedFields) { // 입력된 필드들 변경감지 사용하여 업데이트
      switch (validatedField) {
        case "email":
          if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailDuplicatedException();
          }
          findUser.changeEmail(request.getEmail());
          break;
        case "nickname":
          if (userRepository.existsByNickname(request.getNickname())) {
            throw new NicknameDuplicatedException();
          }
          findUser.changeNickname(request.getNickname());
          break;
        case "name":
          findUser.changeName(request.getName());
          break;
        case "birthday":
          findUser.changeBirthday(request.getBirthday());
          break;
        case "newPassword":
          findUser.changePassword(passwordEncoder.encode(request.getNewPassword()));
          break;
      }
    }
    findUser.setModifiedAt(LocalDateTime.now());
    return new UserResponseWithModifiedAt(findUser);
  }

}
