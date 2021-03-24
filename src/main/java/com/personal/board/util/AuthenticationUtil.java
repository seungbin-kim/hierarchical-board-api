package com.personal.board.util;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public class AuthenticationUtil {

  public static void checkUserId(Long id, Authentication authentication) {
    Long currentUserId = Long.parseLong(authentication.getName());
    if (!id.equals(currentUserId)) {
      throw new AccessDeniedException("요청 id와 인증 id가 다름");
    }
  }

  public static void checkAdmin(Authentication authentication) {
    boolean isAdmin = authentication.getAuthorities().stream()
        .map(Object::toString)
        .anyMatch(Object -> Object.equals("ROLE_ADMIN"));
    if (!isAdmin) {
      throw new AccessDeniedException("관리자가 아님");
    }
  }

}
