package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.LoginDto;
import org.weviewapp.dto.RegisterDto;
import org.weviewapp.repository.RoleRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.security.JwtTokenProvider;
import org.weviewapp.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Override
    public String login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    @Override
    public String register(RegisterDto registerDto) {

//        // add check for username exists in database
//        if(userRepository.existsByUsername(registerDto.getUsername())){
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Username is already exists!.");
//        }
//
//        // add check for email exists in database
//        if(userRepository.existsByEmail(registerDto.getEmail())){
//            throw new BlogAPIException(HttpStatus.BAD_REQUEST, "Email is already exists!.");
//        }
//
//        User user = new User();
//        user.setName(registerDto.getName());
//        user.setUsername(registerDto.getUsername());
//        user.setEmail(registerDto.getEmail());
//        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
//
//        Set<Role> roles = new HashSet<>();
//        Role userRole = roleRepository.findByName("ROLE_USER").get();
//        roles.add(userRole);
//        user.setRoles(roles);

//        userRepository.save(user);

        return "User registered successfully!.";
    }
}
