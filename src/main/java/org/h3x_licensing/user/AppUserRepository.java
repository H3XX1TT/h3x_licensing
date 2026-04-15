package org.h3x_licensing.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);

    boolean existsByEmailIgnoreCase(String email);
}

