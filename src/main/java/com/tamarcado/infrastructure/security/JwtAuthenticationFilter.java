package com.tamarcado.infrastructure.security;

import com.tamarcado.shared.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = SecurityUtils.getTokenFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {
            try {
                String username = tokenProvider.getUsernameFromToken(token);
                String authorities = tokenProvider.getAuthoritiesFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Carrega o usuário do banco de dados
                    var userDetails = userDetailsService.loadUserByUsername(username);

                    // Cria authorities a partir do token
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities != null
                            ? Arrays.stream(authorities.split(","))
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                            : List.of();

                    // Cria autenticação
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    grantedAuthorities
                            );

                    // Define a autenticação no contexto
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("Erro ao processar token JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}