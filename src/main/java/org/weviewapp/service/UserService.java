package org.weviewapp.service;

import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.User;

import java.util.UUID;

public interface UserService {
    public User uploadUserImage(MultipartFile file);
    public User modifyPoints(UUID userId, Integer points);
    public User verifyUser();
    public User getCurrentUser();
    public UserDTO mapUserToDTO(User user);
}
