package org.example.fasthost.config;//package org.example.fasthost.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.antlr.v4.runtime.misc.NotNull;
import org.example.fasthost.entity.Users;
import org.example.fasthost.repository.UsersRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CookieAuthenticationFilter extends OncePerRequestFilter {

    private final UsersRepository usersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("Processing request: {}", requestURI);

        String token = getTokenFromCookie(request);

        if (token != null) {
            log.debug("Token topildi: {}", token.substring(0, Math.min(8, token.length())) + "...");

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<Users> userOpt = usersRepository.findByKey(token);

                if (userOpt.isPresent() && userOpt.get().isActive()) {
                    Users user = userOpt.get();
                    log.info("Foydalanuvchi autentifikatsiya qilindi: {}", user.getName());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication context o'rnatildi");
                } else {
                    log.warn("Token yaroqsiz yoki user bloklangan");
                }
            }
        } else {
            log.debug("Token topilmadi");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    log.debug("AUTH_TOKEN cookie topildi");
                    return cookie.getValue();
                }
            }
        }
        log.debug("Cookies yo'q yoki AUTH_TOKEN topilmadi");
        return null;
    }
}