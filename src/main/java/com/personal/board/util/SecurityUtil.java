package com.personal.board.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

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


  public static boolean isSameUser(final Long id) {
    Long currentUserId = Long.parseLong(getAuthentication().getName());
    return id.equals(currentUserId);
  }


  public static boolean isAdmin() {
    return getAuthentication().getAuthorities().stream()
        .map(Object::toString)
        .anyMatch(Object -> Object.equals("ROLE_ADMIN"));
  }


  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }


  public static void checkAdminAndUserAuthentication(final Long id) {
    if (!SecurityUtil.isAdmin()) {
      if (!SecurityUtil.isSameUser(id)) {
        throw new AccessDeniedException("같은 유저가 아님");
      }
    }
  }

}
