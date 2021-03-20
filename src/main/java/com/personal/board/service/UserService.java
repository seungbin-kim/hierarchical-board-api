package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
import com.personal.board.dto.response.PageDto;
import com.personal.board.dto.response.user.UserResponseWithCreatedAt;
import com.personal.board.dto.response.user.UserResponseWithDate;
import com.personal.board.dto.response.user.UserResponseWithModifiedAt;
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
    if (userRepository.checkUserEmail(request.getEmail())) {
      throw new EmailDuplicatedException();
    }
    if (userRepository.checkUserNickname(request.getNickname())) {
      throw new NicknameDuplicatedException();
    }

    User user = User.createUser(
        request.getEmail(),
        request.getNickname(),
        request.getName(),
        request.getBirthday(),
        request.getPassword());

    User savedUser = userRepository.save(user);
    return new UserResponseWithCreatedAt(savedUser);
  }


  public void deleteUser(final Long userId) {
    Optional<User> userById = userRepository.findUserById(userId);
    if (userById.isEmpty()) {
      throw new UserNotFoundException();
    }
    userRepository.deleteUser(userById.get());
  }


  @Transactional(readOnly = true)
  public UserResponseWithDate getUser(final Long userId) {
    Optional<User> userById = userRepository.findUserById(userId);
    if (userById.isEmpty()) {
      throw new UserNotFoundException();
    }
    return new UserResponseWithDate(userById.get());
  }


  @Transactional(readOnly = true)
  public PageDto<UserResponseWithDate> getPageableUsers(final int size, final int page) { // 조회한 유저들 Dto로 만들어서 반환
    List<UserResponseWithDate> result = userRepository.findPageableUsers(size, page)
        .stream()
        .map(UserResponseWithDate::new)
        .collect(Collectors.toList());

    int totalUserCount = userRepository.getUserCount();
    int totalPages = (totalUserCount / size) - 1;
    if (totalUserCount % size != 0) {
      totalPages++;
    }
    boolean isFirst = (page == 0);
    boolean isLast = (page == totalPages);

    return new PageDto<>(result, totalUserCount, size, totalPages, page, isFirst, isLast);
  }


  public UserResponseWithModifiedAt updateUser(final UserUpdateRequest request, final Long postId) throws IllegalAccessException {
    // 정보 찾아오기
    Optional<User> userById = userRepository.findUserById(postId);
    userById.orElseThrow(UserNotFoundException::new);
    User findUser = userById.get();

    // Password 체크
    if (!findUser.getPassword().equals(request.getPassword())) {
      throw new PasswordIncorrectException();
    }

    // 들어온 값 확인
    Field[] declaredFields = request.getClass().getDeclaredFields();
    ArrayList<String> validatedFields = PatchUtil.validateFields(request, declaredFields); // 입력필드들 얻기

    // 변경감지
    for (String validatedField : validatedFields) { // 입력된 필드들 변경감지 사용하여 업데이트
      switch (validatedField) {
        case "email":
          if (userRepository.checkUserEmail(request.getEmail())) {
            throw new EmailDuplicatedException();
          }
          findUser.changeEmail(request.getEmail());
          break;
        case "nickname":
          if (userRepository.checkUserNickname(request.getEmail())) {
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
          findUser.changePassword(request.getNewPassword());
          break;
      }
    }
    findUser.setModifiedAt(LocalDateTime.now());
    return new UserResponseWithModifiedAt(findUser);
  }

}
