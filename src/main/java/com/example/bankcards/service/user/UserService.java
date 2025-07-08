package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.user.Role;
import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    void deleteUser(Long id);
    UserDto updateUserRole(Long id, Role role);
}
