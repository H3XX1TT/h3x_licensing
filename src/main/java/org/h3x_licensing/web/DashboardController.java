package org.h3x_licensing.web;

import jakarta.validation.Valid;
import org.h3x_licensing.license.LicenseService;
import org.h3x_licensing.product.ProductService;
import org.h3x_licensing.user.AppUser;
import org.h3x_licensing.user.AppUserService;
import org.h3x_licensing.web.dto.ClaimLicenseForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final AppUserService appUserService;
    private final ProductService productService;
    private final LicenseService licenseService;

    public DashboardController(AppUserService appUserService, ProductService productService, LicenseService licenseService) {
        this.appUserService = appUserService;
        this.productService = productService;
        this.licenseService = licenseService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        AppUser currentUser = appUserService.getRequiredByEmail(authentication.getName());
        if (!model.containsAttribute("claimLicenseForm")) {
            model.addAttribute("claimLicenseForm", new ClaimLicenseForm(currentUser.getEmail(), ""));
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("productCount", productService.countProducts());
        model.addAttribute("licenseCount", licenseService.countLicenses());
        model.addAttribute("ownedLicenses", licenseService.listForOwner(currentUser));
        return "dashboard";
    }

    @PostMapping("/dashboard/licenses/claim")
    public String claimLicense(
            Authentication authentication,
            @Valid @ModelAttribute("claimLicenseForm") ClaimLicenseForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.claimLicenseForm", bindingResult);
            redirectAttributes.addFlashAttribute("claimLicenseForm", form);
            return "redirect:/dashboard";
        }

        try {
            AppUser currentUser = appUserService.getRequiredByEmail(authentication.getName());
            licenseService.claimLicense(form, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "License claimed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("claimLicenseForm", form);
        }
        return "redirect:/dashboard";
    }
}

