CREATE INDEX idx_licenses_product_id ON licenses (product_id);
CREATE INDEX idx_licenses_owner_id ON licenses (owner_id);
CREATE INDEX idx_licenses_status ON licenses (status);
CREATE INDEX idx_licenses_last_validated_at ON licenses (last_validated_at);
CREATE INDEX idx_license_audit_logs_license_created ON license_audit_logs (license_id, created_at);

