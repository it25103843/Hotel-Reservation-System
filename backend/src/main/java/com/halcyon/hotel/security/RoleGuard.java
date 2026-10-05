package com.halcyon.hotel.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

public final class RoleGuard {

    private RoleGuard() {}

    /** Throws 401 if there's no authenticated user, 403 if their role isn't in {@code allowed}. */
    public static String requireRole(HttpServletRequest request, String... allowed) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
        }
        if (Arrays.stream(allowed).noneMatch(role::equals)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to do that");
        }
        return role;
    }

    /** Throws 401 if there's no authenticated user; returns their account id. */
    public static Long requireAuth(HttpServletRequest request) {
        Long id = (Long) request.getAttribute("accountId");
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in required");
        }
        return id;
    }
}
