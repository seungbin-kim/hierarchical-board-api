package com.personal.board.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

  private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

  public static Optional<Long> getCurrentUserId() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

}
