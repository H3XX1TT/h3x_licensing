package org.h3x_licensing.config;

import org.h3x_licensing.product.Product;
import org.h3x_licensing.product.ProductRepository;
import org.h3x_licensing.user.AppUser;
import org.h3x_licensing.user.AppUserRepository;
import org.h3x_licensing.user.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    @ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true")
    ApplicationRunner bootstrapData(
            AppUserRepository appUserRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin-email}") String adminEmail,
            @Value("${app.bootstrap.admin-password}") String adminPassword,
            @Value("${app.bootstrap.admin-name}") String adminName
    ) {
        return args -> {
            if (!appUserRepository.existsByEmailIgnoreCase(adminEmail)) {
                AppUser admin = new AppUser();
                admin.setEmail(adminEmail.toLowerCase());
                admin.setDisplayName(adminName);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                admin.setEnabled(true);
                appUserRepository.save(admin);
            }

            if (productRepository.count() == 0) {
                Product product = new Product();
                product.setName("FiveM License Core");
                product.setSlug("fivem-license-core");
                product.setDescription("Sample product for Discord and FiveM license validation flows.");
                product.setActive(true);
                productRepository.save(product);
            }
        };
    }
}

