package org.h3x_licensing.web;

import jakarta.validation.Valid;
import org.h3x_licensing.license.LicenseService;
import org.h3x_licensing.license.LicenseSource;
import org.h3x_licensing.license.LicenseStatus;
import org.h3x_licensing.product.ProductService;
import org.h3x_licensing.user.AppUserService;
import org.h3x_licensing.web.dto.CreateLicenseForm;
import org.h3x_licensing.web.dto.ProductForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final ProductService productService;
    private final LicenseService licenseService;
    private final AppUserService appUserService;

    public AdminController(ProductService productService, LicenseService licenseService, AppUserService appUserService) {
        this.productService = productService;
        this.licenseService = licenseService;
        this.appUserService = appUserService;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductForm("", "", ""));
        }
        if (!model.containsAttribute("createLicenseForm")) {
            model.addAttribute("createLicenseForm", new CreateLicenseForm(null, "", "", "", LicenseSource.DIRECT, LicenseStatus.PENDING, "", ""));
        }
        model.addAttribute("products", productService.listAll());
        model.addAttribute("licenses", licenseService.listAll());
        model.addAttribute("usersCount", appUserService.countUsers());
        model.addAttribute("licenseStatuses", LicenseStatus.values());
        model.addAttribute("licenseSources", LicenseSource.values());
        return "admin";
    }

    @PostMapping("/admin/products")
    public String createProduct(
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productForm", bindingResult);
            redirectAttributes.addFlashAttribute("productForm", form);
            return "redirect:/admin";
        }

        try {
            productService.createProduct(form);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("productForm", form);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/licenses")
    public String createLicense(
            @Valid @ModelAttribute("createLicenseForm") CreateLicenseForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createLicenseForm", bindingResult);
            redirectAttributes.addFlashAttribute("createLicenseForm", form);
            return "redirect:/admin";
        }

        try {
            licenseService.createLicense(form);
            redirectAttributes.addFlashAttribute("successMessage", "License created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("createLicenseForm", form);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/licenses/{licenseId}/status")
    public String updateStatus(
            @PathVariable Long licenseId,
            @RequestParam LicenseStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            licenseService.updateStatus(licenseId, status);
            redirectAttributes.addFlashAttribute("successMessage", "License status updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin";
    }
}

