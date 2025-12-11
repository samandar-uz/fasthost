package org.example.fasthost.config;

import lombok.RequiredArgsConstructor;
import org.example.fasthost.entity.Users;
import org.example.fasthost.repository.UsersRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@Component
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UsersRepository usersRepository;

    @ModelAttribute("user")
    public Users globalUser(
            @CookieValue(value = "AUTH_TOKEN", required = false) String token
    ) {
        if (token == null) return null;

        return usersRepository.findByKey(token).orElse(null);
    }
}
