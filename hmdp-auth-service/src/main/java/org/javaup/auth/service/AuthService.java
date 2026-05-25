package org.javaup.auth.service;

import jakarta.servlet.http.HttpSession;
import org.javaup.dto.LoginFormDTO;
import org.javaup.dto.Result;

public interface AuthService {

    Result<String> sendCode(String phone, HttpSession session);

    Result<String> login(LoginFormDTO loginForm, HttpSession session);
}
