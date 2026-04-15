package org.h3x_licensing.user;

import org.h3x_licensing.web.dto.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        String normalizedEmail = normalizeEmail(form.email());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(form.displayName().trim());
        user.setDiscordId(blankToNull(form.discordId()));
        user.setPasswordHash(passwordEncoder.encode(form.password()));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser getRequiredByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return appUserRepository.count();
    }

    @Transactional
    public AppUser upsertDiscordOAuthUser(Map<String, Object> attributes) {
        String subject = stringValue(attributes.get("id"));
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Discord OAuth response does not contain a valid user id.");
        }

        String email = normalizeEmail(stringValue(attributes.getOrDefault("email", subject + "@discord.local")));
        String username = stringValue(attributes.getOrDefault("username", "Discord User"));

        AppUser user = appUserRepository.findByOauthProviderAndOauthSubject("discord", subject)
                .orElseGet(() -> appUserRepository.findByEmailIgnoreCase(email).orElseGet(AppUser::new));

        user.setEmail(email);
        user.setDisplayName(username.isBlank() ? "Discord User" : username.trim());
        user.setDiscordId(subject);
        user.setOauthProvider("discord");
        user.setOauthSubject(subject);
        user.setEnabled(true);
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        }

        return appUserRepository.save(user);
    }

    @Transactional
    public void updatePassword(AppUser user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUserRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}

