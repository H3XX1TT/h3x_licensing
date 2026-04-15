package org.h3x_licensing.license;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.h3x_licensing.product.Product;
import org.h3x_licensing.user.AppUser;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_key", nullable = false, unique = true, length = 80)
    private String licenseKey;

    @Column(name = "purchase_reference", nullable = false, unique = true, length = 120)
    private String purchaseReference;

    @Column(name = "buyer_name", nullable = false, length = 120)
    private String buyerName;

    @Column(name = "buyer_email", nullable = false, length = 190)
    private String buyerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LicenseSource source = LicenseSource.DIRECT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LicenseStatus status = LicenseStatus.PENDING;

    @Column(name = "discord_id", length = 120)
    private String discordId;

    @Column(name = "fivem_server_id", length = 120)
    private String fivemServerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (licenseKey == null || licenseKey.isBlank()) {
            licenseKey = UUID.randomUUID().toString();
        }
    }

    public Long getId() {
        return id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getPurchaseReference() {
        return purchaseReference;
    }

    public void setPurchaseReference(String purchaseReference) {
        this.purchaseReference = purchaseReference;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public LicenseSource getSource() {
        return source;
    }

    public void setSource(LicenseSource source) {
        this.source = source;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    public void setStatus(LicenseStatus status) {
        this.status = status;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getFivemServerId() {
        return fivemServerId;
    }

    public void setFivemServerId(String fivemServerId) {
        this.fivemServerId = fivemServerId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public LocalDateTime getLastValidatedAt() {
        return lastValidatedAt;
    }

    public void setLastValidatedAt(LocalDateTime lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

