package org.weviewapp.service;

import org.weviewapp.dto.LoginDTO;
import org.weviewapp.dto.RegisterDTO;

public interface AuthService {
    String login(LoginDTO loginDto);

    String register(RegisterDTO registerDto);
}