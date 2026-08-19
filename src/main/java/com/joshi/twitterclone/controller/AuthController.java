package com.joshi.twitterclone.controller;

import com.joshi.twitterclone.dto.RegisterRequest;
import com.joshi.twitterclone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Renders the login page.
     * Note: Spring Security automatically handles POST /login via its filter chain.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Renders the registration page with an empty form backing object.
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    /**
     * Validates and registers a new user.
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                      BindingResult bindingResult) {
        if (userService.usernameExists(request.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username is already taken");
        }

        if (userService.emailExists(request.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email is already registered");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.registerUser(request);
        return "redirect:/login?registered";
    }
}