package org.example.fasthost.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.fasthost.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    @PostMapping("/sign_up")
    public String sign_up(
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam String email,
            HttpServletResponse response,
            RedirectAttributes redirect
    ) {
        var result = authService.sign_up(name, password, email, response);

        if (!result.isSuccess()) {
            redirect.addFlashAttribute("error", result.getMessage());
            return "redirect:/auth";
        }
        return "redirect:/index";
    }


    @PostMapping("/sign_in")
    public String sign_in(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes redirect
    ) {
        var result = authService.sign_in(email, password, response);

        if (!result.isSuccess()) {
            redirect.addFlashAttribute("error", result.getMessage());
            return "redirect:/auth";
        }
        return "redirect:/index";
    }

}
