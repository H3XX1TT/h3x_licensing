package org.h3x_licensing.config;

import org.h3x_licensing.user.AppUser;
import org.h3x_licensing.user.AppUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final AppUserService appUserService;

    public DiscordOAuth2UserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"discord".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"), "Only discord OAuth is configured.");
        }

        AppUser user = appUserService.upsertDiscordOAuthUser(oauth2User.getAttributes());

        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("email", user.getEmail());
        attributes.put("name", user.getDisplayName());

        return new DefaultOAuth2User(
                Set.of(() -> "ROLE_" + user.getRole().name()),
                attributes,
                "email"
        );
    }
}

