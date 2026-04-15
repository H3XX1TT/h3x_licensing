package org.h3x_licensing.license;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseAuditLogRepository extends JpaRepository<LicenseAuditLog, Long> {
}

