package org.h3x_licensing.web;

import jakarta.validation.Valid;
import org.h3x_licensing.config.AppSecurityProperties;
import org.h3x_licensing.user.AppUserService;
import org.h3x_licensing.user.PasswordResetService;
import org.h3x_licensing.web.dto.ForgotPasswordForm;
import org.h3x_licensing.web.dto.RegistrationForm;
import org.h3x_licensing.web.dto.ResetPasswordForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AppUserService appUserService;
    private final AppSecurityProperties appSecurityProperties;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AppUserService appUserService,
            AppSecurityProperties appSecurityProperties,
            PasswordResetService passwordResetService
    ) {
        this.appUserService = appUserService;
        this.appSecurityProperties = appSecurityProperties;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordForm")) {
            model.addAttribute("forgotPasswordForm", new ForgotPasswordForm(""));
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.forgotPasswordForm", bindingResult);
            redirectAttributes.addFlashAttribute("forgotPasswordForm", form);
            return "redirect:/forgot-password";
        }

        passwordResetService.requestPasswordReset(form.email());
        redirectAttributes.addFlashAttribute("successMessage", "If the email exists, a reset link was sent.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        if (!passwordResetService.isResetTokenValid(token)) {
            model.addAttribute("errorMessage", "The password reset link is invalid or expired.");
            return "login";
        }

        if (!model.containsAttribute("resetPasswordForm")) {
            model.addAttribute("resetPasswordForm", new ResetPasswordForm(token, "", ""));
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.resetPasswordForm", bindingResult);
            redirectAttributes.addFlashAttribute("resetPasswordForm", form);
            return "redirect:/reset-password?token=" + form.token();
        }

        if (!form.password().equals(form.confirmPassword())) {
            redirectAttributes.addFlashAttribute("resetPasswordForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Password confirmation does not match.");
            return "redirect:/reset-password?token=" + form.token();
        }

        try {
            passwordResetService.resetPassword(form.token(), form.password());
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully. Please sign in.");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!appSecurityProperties.getAuth().isRegistrationEnabled()) {
            model.addAttribute("errorMessage", "Public registration is currently disabled. Please contact support.");
            return "login";
        }
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm("", "", "", ""));
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (!appSecurityProperties.getAuth().isRegistrationEnabled()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Public registration is currently disabled. Please contact support.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registrationForm", bindingResult);
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }

        try {
            appUserService.register(form);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("registrationForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Registration completed. You can now sign in.");
        return "redirect:/login";
    }
}

