package org.h3x_licensing.config;

import org.h3x_licensing.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            DiscordOAuth2UserService discordOAuth2UserService,
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error").permitAll()
                        .requestMatchers("/register", "/forgot-password", "/reset-password", "/login/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/v1/licenses/validate").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(discordOAuth2UserService)))
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/**", "/h2-console/**"))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
                        .frameOptions(frame -> {
                            if (h2ConsoleEnabled) {
                                frame.sameOrigin();
                            } else {
                                frame.deny();
                            }
                        })
                        .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                )
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
        return username -> appUserRepository.findByEmailIgnoreCase(username)
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .disabled(!user.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

