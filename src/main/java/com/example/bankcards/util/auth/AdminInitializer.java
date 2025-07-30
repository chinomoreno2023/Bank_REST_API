package com.example.bankcards.util.auth;

import com.example.bankcards.config.security.AdminProperties;
import com.example.bankcards.dto.auth.LoginAndPasswordAuthRequest;
import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.validation.UsernameAlreadyExistsException;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.service.auth.JwtAuthService;
import com.example.bankcards.service.card.AdminCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {
    private final JwtAuthService jwtAuthService;
    private final UserRepository userRepository;
    private final AdminCardService adminCardService;
    private final AdminProperties adminProperties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void createInitialAdminUser() {
        final String adminUsername = adminProperties.getUsername();
        final String adminPassword = adminProperties.getPassword();

        log.info("Attempting to initialize admin user",
                keyValue("username", adminUsername)
        );

        Optional<User> existingAdminOptional = userRepository.findByUsername(adminUsername);
        if (existingAdminOptional.isPresent()) {
            log.info("Admin user already exists, skipping initialization process",
                    keyValue("username", adminUsername)
            );
            return;
        }

        LoginAndPasswordAuthRequest adminRequest = new LoginAndPasswordAuthRequest(adminUsername, adminPassword);
        User newAdminUser;

        try {
            newAdminUser = jwtAuthService.register(adminRequest);
            newAdminUser.setRole(Role.ADMIN);
            userRepository.save(newAdminUser);
            log.info("New admin user registered successfully", keyValue("username", adminUsername));

            addCardsToAdmin(newAdminUser.getId());
            log.info("Admin cards created for new user", keyValue("username", adminUsername));

        } catch (UsernameAlreadyExistsException e) {
            log.warn("Admin user already existed during registration attempt (race condition likely), skipping card creation",
                    keyValue("username", adminUsername),
                    e
            );
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to initialize admin user due to data integrity violation (unexpected after initial check)",
                    keyValue("username", adminUsername),
                    keyValue("error", e.getMessage()), e
            );
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred during admin user initialization",
                    keyValue("username", adminUsername),
                    keyValue("error", e.getMessage()), e);
            throw new RuntimeException("Admin initialization failed unexpectedly", e);
        }
    }

    private void addCardsToAdmin(Long adminUserId) {
        adminCardService.createCardForUser(
                new CardCreateRequest(
                        adminUserId,
                        adminProperties.getFirstCardNumber(),
                        adminProperties.getInitialCardsExpiration())
        );

        adminCardService.createCardForUser(
                new CardCreateRequest(
                        adminUserId,
                        adminProperties.getSecondCardNumber(),
                        adminProperties.getInitialCardsExpiration())
        );
    }
}
