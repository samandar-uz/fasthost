package org.example.fasthost.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.fasthost.repository.TariffsRepository;
import org.example.fasthost.repository.UsersRepository;
import org.example.fasthost.service.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final TariffsRepository tariffsRepository;
    private final UsersRepository usersRepository;

    @GetMapping("/")
    public String root() {
        return "home";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/hosting")
    public String hosting(Model model) {
        model.addAttribute("plans", tariffsRepository.findAllByActiveTrue());
        return "hosting";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping("/auth")
    public String authPage(@CookieValue(value = "AUTH_TOKEN", required = false) String token) {
        if (token != null && usersRepository.findByKey(token).isPresent()) {
            return "redirect:/index";
        }
        return "auth";
    }


}
