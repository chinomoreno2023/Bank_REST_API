package com.example.bankcards.dto.user;

import com.example.bankcards.entity.user.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateRoleRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "Role is required")
    private Role role;
}
