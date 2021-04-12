package com.personal.board.service;

import com.personal.board.dto.request.SignUpRequest;
import com.personal.board.dto.request.UserUpdateRequest;
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
import com.personal.board.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final CommentRepository commentRepository;

  private final PostRepository postRepository;

  private final PasswordEncoder passwordEncoder;

  private final PatchUtil patchUtil;


  /**
   * 회원가입
   *
   * @param request 등록할 회원정보 객체
   * @return 등록한 회원
   */
  @Transactional
  public UserResponseWithCreatedAt signUp(final SignUpRequest request) {

    checkDuplicate(request.getEmail(), request.getNickname());

    User user = createUser(request);
    User savedUser = userRepository.save(user);
    return new UserResponseWithCreatedAt(savedUser);
  }


  /**
   * 회원탈퇴
   *
   * @param userId 탈퇴할 회원 id
   */
  @Transactional
  public void deleteUser(final Long userId) {

    User user = findUser(userId);

    commentRepository.updateWriterIdToNull(userId);
    postRepository.updateWriterIdToNull(userId);

    userRepository.delete(user);
  }


  /**
   * 회원 단건조회
   *
   * @param userId 조회할 회원 id
   * @return 조회된 회원
   */
  public UserResponseWithDate getUser(final Long userId) {

    return new UserResponseWithDate(findUser(userId));
  }


  /**
   * 회원목록 페이징 조회
   *
   * @param pageable 페이징 정보
   * @return 페이징된 유저목록
   */
  public Page<UserResponseWithDate> getPageableUsers(final Pageable pageable) {

    Page<User> userPage = userRepository.findAll(pageable);
    return userPage.map(UserResponseWithDate::new);
  }


  /**
   * 회원 업데이트
   *
   * @param request 업데이트 정보
   * @param userId  대상 회원 아이디
   * @return 업데이트한 회원
   * @throws IllegalAccessException 필드 접근 불가시 발생
   */
  @Transactional
  public UserResponseWithModifiedAt updateUser(final UserUpdateRequest request,
                                               final Long userId) throws IllegalAccessException {

    User findUser = findUser(userId);
    checkPassword(request.getPassword(), findUser.getPassword());

    ArrayList<String> validatedFields = patchUtil.getValidatedFields(request);
    update(request, findUser, validatedFields);

    return new UserResponseWithModifiedAt(findUser);
  }


  /**
   * 회원 조회
   *
   * @param userId 조회할 회원 id
   * @return 조회된 회원
   */
  User findUser(final Long userId) {

    Optional<User> userById = userRepository.findById(userId);
    userById.orElseThrow(UserNotFoundException::new);
    return userById.get();
  }


  /**
   * 비밀번호 확인
   *
   * @param requestPassword 입력된 비밀번호
   * @param userPassword    DB에 저장된 유저의 비밀번호
   */
  private void checkPassword(final String requestPassword,
                             final String userPassword) {

    if (!passwordEncoder.matches(requestPassword, userPassword)) {
      throw new PasswordIncorrectException();
    }
  }


  /**
   * 이메일, 닉네임 중복검사
   *
   * @param email    이메일
   * @param nickname 닉네임
   */
  private void checkDuplicate(final String email,
                              final String nickname) {

    if (userRepository.existsByEmail(email)) {
      throw new EmailDuplicatedException();
    }
    if (userRepository.existsByNickname(nickname)) {
      throw new NicknameDuplicatedException();
    }
  }


  /**
   * 회원엔티티 생성
   *
   * @param request 등록 요청정보
   * @return 요청 정보로 생성한 회원 엔티티
   */
  private User createUser(final SignUpRequest request) {

    Authority authority = new Authority(Role.ROLE_USER);
    return User.createUser(
        request.getEmail(),
        request.getNickname(),
        request.getName(),
        request.getBirthday(),
        passwordEncoder.encode(request.getPassword()),
        authority);
  }


  /**
   * 회원정보 업데이트
   *
   * @param request         업데이트 요청정보
   * @param findUser        업데이트 할 회원
   * @param validatedFields 업데이트 할 필드
   */
  private void update(final UserUpdateRequest request,
                      final User findUser,
                      final ArrayList<String> validatedFields) {

    for (String validatedField : validatedFields) {
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
  }

}
