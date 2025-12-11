package org.example.fasthost.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.fasthost.entity.Users;
import org.example.fasthost.entity.dto.Response;
import org.example.fasthost.repository.UsersRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HomeService homeService;

    public Response<Void> sign_up(String name, String password, String email, HttpServletResponse response) {

        if (userRepository.existsByEmail(email)) {
            return Response.error("Email allaqachon ro'yxatdan o'tgan!");
        }

        String encodedPassword = passwordEncoder.encode(password);
        String token = homeService.generateToken();
        Users user = new Users();
        user.setName(name);
        user.setActive(true);

        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setKey(token);
        userRepository.save(user);


        homeService.addAuthCookie(response, token);
        return Response.<Void>builder()
                .success(true)
                .message("Ro'yxatdan o'tish muvaffaqiyatli!")
                .build();
    }


    public Response<Users> sign_in(String email, String password, HttpServletResponse response) {


        Users user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return Response.error("Email ro'yxatdan o'tmagan!");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Response.error("Parol noto'g'ri!");
        }
        String token = homeService.generateToken();
        user.setKey(token);
        userRepository.save(user);
        homeService.addAuthCookie(response, token);
        return Response.<Users>builder()
                .success(true)
                .message("Login successful!")
                .data(user)
                .build();
    }

    public void logout(HttpServletResponse response) {
        homeService.removeAuthCookie(response);
    }
}
