package org.h3x_licensing.user;

import org.h3x_licensing.config.AppMailProperties;
import org.h3x_licensing.config.AppUrlProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AppUserService appUserService;
    private final JavaMailSender javaMailSender;
    private final AppMailProperties appMailProperties;
    private final AppUrlProperties appUrlProperties;
    private final boolean mailEnabled;

    public PasswordResetService(
            AppUserRepository appUserRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            AppUserService appUserService,
            @Autowired(required = false) JavaMailSender javaMailSender,
            AppMailProperties appMailProperties,
            AppUrlProperties appUrlProperties,
            @Value("${app.mail.enabled:false}") boolean mailEnabled
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.appUserService = appUserService;
        this.javaMailSender = javaMailSender;
        this.appMailProperties = appMailProperties;
        this.appUrlProperties = appUrlProperties;
        this.mailEnabled = mailEnabled;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<AppUser> optionalUser = appUserRepository.findByEmailIgnoreCase(normalizeEmail(email));
        if (optionalUser.isEmpty()) {
            return;
        }

        AppUser user = optionalUser.get();
        passwordResetTokenRepository.deleteAllByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        passwordResetTokenRepository.save(token);

        if (mailEnabled) {
            sendResetMail(user, token);
        }
    }

    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String tokenValue) {
        return passwordResetTokenRepository.findByToken(tokenValue)
                .map(PasswordResetToken::isActive)
                .orElse(false);
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("The password reset token is invalid."));

        if (!token.isActive()) {
            throw new IllegalArgumentException("The password reset token is expired or already used.");
        }

        appUserService.updatePassword(token.getUser(), newPassword);
        token.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(token);
    }

    private void sendResetMail(AppUser user, PasswordResetToken token) {
        String resetUrl = appUrlProperties.getPublicBase() + "/reset-password?token=" + token.getToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(appMailProperties.getFromAddress());
        mailMessage.setTo(user.getEmail());
        mailMessage.setReplyTo(appMailProperties.getSupportAddress());
        mailMessage.setSubject("Reset your H3X Licensing password");
        mailMessage.setText("Hello " + user.getDisplayName() + ",\n\n" +
                "Use the following link to reset your password. The link is valid for 30 minutes.\n" +
                resetUrl + "\n\n" +
                "If you did not request this reset, you can ignore this email.");
        javaMailSender.send(mailMessage);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

