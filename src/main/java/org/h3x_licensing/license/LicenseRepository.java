package org.h3x_licensing.license;

import org.h3x_licensing.user.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    @EntityGraph(attributePaths = {"product", "owner"})
    List<License> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"product", "owner"})
    List<License> findAllByOwnerOrderByCreatedAtDesc(AppUser owner);

    @EntityGraph(attributePaths = {"product", "owner"})
    Optional<License> findByPurchaseReferenceIgnoreCase(String purchaseReference);

    @EntityGraph(attributePaths = {"product", "owner"})
    Optional<License> findByLicenseKeyAndProduct_SlugIgnoreCase(String licenseKey, String productSlug);

    boolean existsByPurchaseReferenceIgnoreCase(String purchaseReference);
}

