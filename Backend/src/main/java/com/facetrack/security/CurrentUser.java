package com.facetrack.security;

import com.facetrack.entity.User;
import com.facetrack.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility to extract the current authenticated user from SecurityContext.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AuthException("Not authenticated.");
        }
        return user;
    }
}
