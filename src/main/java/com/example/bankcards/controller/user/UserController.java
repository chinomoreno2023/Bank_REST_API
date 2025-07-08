package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.UpdateRoleRequest;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static net.logstash.logback.argument.StructuredArguments.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity
                .ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("getUserById",
                keyValue("user_id", id)
        );
        return ResponseEntity
                .ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Delete user",
                keyValue("user_id", id)
        );
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id,
                                                  @RequestBody @Valid UpdateRoleRequest request) {
        log.info("Update user role",
                keyValue("user_id", id),
                keyValue("new_role", request.getRole())
        );
        return ResponseEntity
                .ok(userService.updateUserRole(id, request.getRole()));
    }
}
