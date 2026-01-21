package com.tamarcado.infrastructure.security;

import com.tamarcado.shared.constant.SecurityConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extrai o token JWT do header Authorization
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Obtém o usuário autenticado atual
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Obtém o username do usuário autenticado atual
     */
    public static String getCurrentUsername() {
        Authentication authentication = getCurrentAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        
        return null;
    }

    /**
     * Verifica se o usuário atual está autenticado
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null 
            && authentication.isAuthenticated()
            && !authentication.getPrincipal().equals("anonymousUser");
    }

    /**
     * Verifica se o usuário atual tem uma role específica
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getCurrentAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        }
        
        return false;
    }
}