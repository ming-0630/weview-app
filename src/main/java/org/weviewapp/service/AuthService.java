package org.weviewapp.service;

import org.weviewapp.dto.LoginDto;
import org.weviewapp.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);

    String register(RegisterDto registerDto);
}