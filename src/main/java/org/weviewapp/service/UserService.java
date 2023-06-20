package org.weviewapp.service;

import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.User;

public interface UserService {
    public User uploadUserImage(MultipartFile file);
    public UserDTO mapUserToDTO(User user);
}
