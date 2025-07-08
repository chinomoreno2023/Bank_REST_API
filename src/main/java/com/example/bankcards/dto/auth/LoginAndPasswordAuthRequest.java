package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginAndPasswordAuthRequest {

    @Email(message = "Email is not valid")
    @NotBlank(message = "Email is required")
    private String username;

    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @NotBlank(message = "Password is required")
    private String password;
}