package org.h3x_licensing;

import org.junit.jupiter.api.Test;
import org.h3x_licensing.license.License;
import org.h3x_licensing.license.LicenseRepository;
import org.h3x_licensing.license.LicenseSource;
import org.h3x_licensing.license.LicenseStatus;
import org.h3x_licensing.product.Product;
import org.h3x_licensing.product.ProductRepository;
import org.h3x_licensing.user.AppUser;
import org.h3x_licensing.user.AppUserRepository;
import org.h3x_licensing.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebPanelIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALUE = "test-validation-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void landingPageShouldRender() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("H3X Licensing Panel")));
    }

    @Test
    void registrationShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "newbuyer@example.com")
                        .param("displayName", "New Buyer")
                        .param("discordId", "99887766")
                        .param("password", "StrongPass123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login"));
    }

    @Test
    void forgotPasswordPageShouldRender() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reset password")));
    }

    @Test
    void forgotPasswordSubmissionShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "unknown@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login"));
    }

    @Test
    void resetPasswordWithInvalidTokenShouldRenderLogin() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("invalid or expired")));
    }

    @Test
    void discordOAuthAuthorizationShouldRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/discord"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("discord.com")));
    }

    @Test
    void invalidLicenseValidationShouldReturnFalse() throws Exception {
        mockMvc.perform(post("/api/v1/licenses/validate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "licenseKey": "missing-key",
                                  "productSlug": "missing-product",
                                  "discordId": "123",
                                  "fivemServerId": "server-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void validationWithoutApiKeyShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/licenses/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "licenseKey": "missing-key",
                                  "productSlug": "missing-product"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("INVALID_API_KEY"));
    }

    @Test
    @WithMockUser(username = "admin@h3x.local", roles = {"ADMIN"})
    void adminPageShouldRenderForAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("License operations")));
    }

    @Test
    void validLicenseValidationShouldReturnTrue() throws Exception {
        Product product = new Product();
        product.setName("Validation Product");
        product.setSlug("validation-product-" + UUID.randomUUID().toString().substring(0, 8));
        product.setDescription("Product used for API validation integration tests.");
        product.setActive(true);
        Product savedProduct = productRepository.save(product);

        License license = new License();
        license.setProduct(savedProduct);
        license.setBuyerName("Validated Buyer");
        license.setBuyerEmail("validated@example.com");
        license.setPurchaseReference("PUR-" + UUID.randomUUID());
        license.setLicenseKey("license-" + UUID.randomUUID());
        license.setSource(LicenseSource.DIRECT);
        license.setStatus(LicenseStatus.ACTIVE);
        licenseRepository.save(license);

        mockMvc.perform(post("/api/v1/licenses/validate")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "licenseKey": "%s",
                                  "productSlug": "%s",
                                  "discordId": "discord-123",
                                  "fivemServerId": "server-01"
                                }
                                """.formatted(license.getLicenseKey(), savedProduct.getSlug())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.productSlug").value(savedProduct.getSlug()));
    }

    @Test
    @WithMockUser(username = "claimbuyer@example.com", roles = {"USER"})
    void claimedLicenseShouldBeAssignedToCurrentUser() throws Exception {
        AppUser user = new AppUser();
        user.setEmail("claimbuyer@example.com");
        user.setDisplayName("Claim Buyer");
        user.setPasswordHash(passwordEncoder.encode("ClaimPass123!"));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        AppUser savedUser = appUserRepository.save(user);

        Product product = new Product();
        product.setName("Claim Product");
        product.setSlug("claim-product-" + UUID.randomUUID().toString().substring(0, 8));
        product.setDescription("Product used for claim integration tests.");
        product.setActive(true);
        Product savedProduct = productRepository.save(product);

        License license = new License();
        license.setProduct(savedProduct);
        license.setBuyerName("Claim Buyer");
        license.setBuyerEmail(savedUser.getEmail());
        license.setPurchaseReference("CLAIM-" + UUID.randomUUID());
        license.setLicenseKey("claim-license-" + UUID.randomUUID());
        license.setSource(LicenseSource.MANUAL);
        license.setStatus(LicenseStatus.PENDING);
        License savedLicense = licenseRepository.save(license);

        mockMvc.perform(post("/dashboard/licenses/claim")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("buyerEmail", savedUser.getEmail())
                        .param("purchaseReference", savedLicense.getPurchaseReference()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/dashboard"));

        License claimedLicense = licenseRepository.findById(savedLicense.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(savedUser.getId(), claimedLicense.getOwner().getId());
        org.junit.jupiter.api.Assertions.assertEquals(LicenseStatus.ACTIVE, claimedLicense.getStatus());
    }
}



