package org.example.fasthost.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HomeService {

    @Value("${app.cookie.name:AUTH_TOKEN}")
    private String cookieName;

    @Value("${app.cookie.max-age:604800}")
    private int cookieMaxAge;

    @Value("${app.cookie.http-only:true}")
    private boolean cookieHttpOnly;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.path:/}")
    private String cookiePath;

    public void addAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
    }

    public void removeAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(cookieHttpOnly);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}