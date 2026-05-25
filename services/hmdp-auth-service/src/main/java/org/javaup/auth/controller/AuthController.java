package org.javaup.auth.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.javaup.auth.service.AuthService;
import org.javaup.dto.LoginFormDTO;
import org.javaup.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam("phone") String phone, HttpSession session) {
        return authService.sendCode(phone, session);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        return authService.login(loginForm, session);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "authorization", required = false) String token) {
        return authService.logout(token);
    }

    @GetMapping("/me")
    public Result<?> me(@RequestHeader(value = "authorization", required = false) String token) {
        return authService.me(token);
    }
}
