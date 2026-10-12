package com.yuvoride.backend.security;

import com.yuvoride.backend.entity.User;
import com.yuvoride.backend.enums.UserStatus;
import com.yuvoride.backend.exception.UnauthorizedException;
import com.yuvoride.backend.repository.UserRepository;
import com.yuvoride.backend.util.JsonResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Long userId = jwtService.parseUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Invalid access token"));
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Account is not active");
            }
            UserPrincipal principal = new UserPrincipal(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException ex) {
            JsonResponses.write(response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }
}
