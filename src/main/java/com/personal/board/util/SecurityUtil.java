package com.personal.board.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

  /**
   * 회원의 id 반환
   * @return SecurityContext 의 회원 id 반환
   */
  public static Optional<Long> getCurrentUserId() {

    final Authentication authentication = getAuthentication();
    if (authentication == null) {
      logger.debug("Security Context에 인증 정보가 없습니다.");
      return Optional.empty();
    }
    Long userId = null;
    if (authentication.getPrincipal() instanceof UserDetails) {
      UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
      userId = Long.parseLong(springSecurityUser.getUsername());
    } else if (authentication.getPrincipal() instanceof String) {
      userId = Long.parseLong((String) authentication.getPrincipal());
    }

    return Optional.ofNullable(userId);
  }


  /**
   * 로그인 한 회원이 관리자인지 검사
   * @return 관리자라면 true, 아니라면 false
   */
  public static boolean isAdmin() {

    return getAuthentication().getAuthorities().stream()
        .map(Object::toString)
        .anyMatch(Object -> Object.equals("ROLE_ADMIN"));
  }


  /**
   * SecurityContext 의 Authentication 반환
   * @return Authentication 반환
   */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }


  /**
   * 관리자 또는 로그인된 회원과 같은지 검사
   * @param id 검사할 회원 id
   */
  public static void checkAdminAndSameUser(final Long id) {

    if (!isAdmin()) {
      if (!isSameUser(id)) {
        throw new AccessDeniedException("같은 유저가 아닙니다.");
      }
    }
  }


  /**
   * 로그인된 회원과 같은지 검사
   * @param id 검사할 회원 id
   * @return 로그인한 회원과 같다면 true, 아니라면 false
   */
  private static boolean isSameUser(final Long id) {

    Long currentUserId = getCurrentUserId().get();
    return id.equals(currentUserId);
  }

}
