package org.h3x_licensing.license;

import org.h3x_licensing.api.LicenseValidationRequest;
import org.h3x_licensing.api.LicenseValidationResponse;
import org.h3x_licensing.product.Product;
import org.h3x_licensing.product.ProductService;
import org.h3x_licensing.user.AppUser;
import org.h3x_licensing.web.dto.ClaimLicenseForm;
import org.h3x_licensing.web.dto.CreateLicenseForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseAuditLogRepository licenseAuditLogRepository;
    private final ProductService productService;

    public LicenseService(
            LicenseRepository licenseRepository,
            LicenseAuditLogRepository licenseAuditLogRepository,
            ProductService productService
    ) {
        this.licenseRepository = licenseRepository;
        this.licenseAuditLogRepository = licenseAuditLogRepository;
        this.productService = productService;
    }

    @Transactional
    public License createLicense(CreateLicenseForm form) {
        String purchaseReference = normalize(form.purchaseReference());
        if (licenseRepository.existsByPurchaseReferenceIgnoreCase(purchaseReference)) {
            throw new IllegalArgumentException("A license with this purchase reference already exists.");
        }

        Product product = productService.getRequiredById(form.productId());

        License license = new License();
        license.setProduct(product);
        license.setBuyerName(form.buyerName().trim());
        license.setBuyerEmail(normalizeEmail(form.buyerEmail()));
        license.setPurchaseReference(purchaseReference);
        license.setSource(form.source());
        license.setStatus(form.status());
        license.setDiscordId(blankToNull(form.discordId()));
        license.setFivemServerId(blankToNull(form.fivemServerId()));

        License savedLicense = licenseRepository.save(license);
        audit(savedLicense, "LICENSE_CREATED", "License created by admin.", null);
        return savedLicense;
    }

    @Transactional
    public License claimLicense(ClaimLicenseForm form, AppUser user) {
        License license = licenseRepository.findByPurchaseReferenceIgnoreCase(normalize(form.purchaseReference()))
                .orElseThrow(() -> new IllegalArgumentException("No license was found for that purchase reference."));

        if (!license.getBuyerEmail().equalsIgnoreCase(normalizeEmail(form.buyerEmail()))) {
            audit(license, "CLAIM_REJECTED", "Claim rejected because the buyer email did not match.", null);
            throw new IllegalArgumentException("The buyer email does not match the stored purchase email.");
        }

        if (license.getStatus() == LicenseStatus.REVOKED) {
            audit(license, "CLAIM_REJECTED", "Claim rejected because the license is revoked.", null);
            throw new IllegalArgumentException("This license has been revoked and cannot be claimed.");
        }

        if (license.getOwner() != null && !license.getOwner().getId().equals(user.getId())) {
            audit(license, "CLAIM_REJECTED", "Claim rejected because the license is already assigned.", null);
            throw new IllegalArgumentException("This license is already assigned to another account.");
        }

        license.setOwner(user);
        if (license.getStatus() == LicenseStatus.PENDING) {
            license.setStatus(LicenseStatus.ACTIVE);
        }

        License savedLicense = licenseRepository.save(license);
        audit(savedLicense, "LICENSE_CLAIMED", "License claimed successfully.", "ownerEmail=" + user.getEmail());
        return savedLicense;
    }

    @Transactional
    public void updateStatus(Long licenseId, LicenseStatus status) {
        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new IllegalArgumentException("License not found."));
        license.setStatus(status);
        licenseRepository.save(license);
        audit(license, "STATUS_UPDATED", "License status changed to " + status + ".", null);
    }

    @Transactional
    public LicenseValidationResponse validateLicense(LicenseValidationRequest request) {
        License license = licenseRepository.findByLicenseKeyAndProduct_SlugIgnoreCase(
                        request.licenseKey().trim(),
                        request.productSlug().trim())
                .orElse(null);

        if (license == null) {
            return invalid("NOT_FOUND", "The license key or product slug is invalid.");
        }

        if (license.getStatus() != LicenseStatus.ACTIVE) {
            audit(license, "VALIDATION_REJECTED", "Validation rejected because the license is not active.", null);
            return invalid("INACTIVE", "The license is not active.");
        }

        String discordId = blankToNull(request.discordId());
        String fivemServerId = blankToNull(request.fivemServerId());

        if (license.getDiscordId() != null && discordId != null && !license.getDiscordId().equals(discordId)) {
            audit(license, "VALIDATION_REJECTED", "Validation rejected due to a Discord ID mismatch.", null);
            return invalid("DISCORD_MISMATCH", "The provided Discord ID does not match this license.");
        }

        if (license.getFivemServerId() != null && fivemServerId != null && !license.getFivemServerId().equals(fivemServerId)) {
            audit(license, "VALIDATION_REJECTED", "Validation rejected due to a FiveM server ID mismatch.", null);
            return invalid("FIVEM_MISMATCH", "The provided FiveM server ID does not match this license.");
        }

        if (license.getDiscordId() == null && discordId != null) {
            license.setDiscordId(discordId);
        }
        if (license.getFivemServerId() == null && fivemServerId != null) {
            license.setFivemServerId(fivemServerId);
        }

        license.setLastValidatedAt(LocalDateTime.now());
        License savedLicense = licenseRepository.save(license);
        audit(savedLicense, "VALIDATION_SUCCESS", "License validated successfully.", metadata(discordId, fivemServerId));

        return new LicenseValidationResponse(
                true,
                savedLicense.getStatus().name(),
                "License validated successfully.",
                savedLicense.getLicenseKey(),
                savedLicense.getProduct().getSlug(),
                maskEmail(savedLicense.getOwner() != null ? savedLicense.getOwner().getEmail() : savedLicense.getBuyerEmail())
        );
    }

    @Transactional(readOnly = true)
    public List<License> listAll() {
        return licenseRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<License> listForOwner(AppUser owner) {
        return licenseRepository.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public long countLicenses() {
        return licenseRepository.count();
    }

    private void audit(License license, String action, String message, String metadata) {
        LicenseAuditLog log = new LicenseAuditLog();
        log.setLicense(license);
        log.setAction(action);
        log.setMessage(message);
        log.setMetadataJson(metadata);
        licenseAuditLogRepository.save(log);
    }

    private LicenseValidationResponse invalid(String status, String message) {
        return new LicenseValidationResponse(false, status, message, null, null, null);
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String metadata(String discordId, String fivemServerId) {
        return "discordId=" + (discordId != null ? discordId : "") + ";fivemServerId=" + (fivemServerId != null ? fivemServerId : "");
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return null;
        }

        String[] parts = email.split("@", 2);
        String localPart = parts[0];
        if (localPart.length() <= 2) {
            return "**@" + parts[1];
        }
        return localPart.substring(0, 2) + "***@" + parts[1];
    }
}

