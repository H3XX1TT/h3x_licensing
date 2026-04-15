package org.h3x_licensing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private final Api api = new Api();
    private final Auth auth = new Auth();

    public Api getApi() {
        return api;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Api {
        private String validationKey;
        private String validationHeader = "X-API-Key";
        private int maxRequestsPerMinute = 60;

        public String getValidationKey() {
            return validationKey;
        }

        public void setValidationKey(String validationKey) {
            this.validationKey = validationKey;
        }

        public String getValidationHeader() {
            return validationHeader;
        }

        public void setValidationHeader(String validationHeader) {
            this.validationHeader = validationHeader;
        }

        public int getMaxRequestsPerMinute() {
            return maxRequestsPerMinute;
        }

        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
            this.maxRequestsPerMinute = maxRequestsPerMinute;
        }
    }

    public static class Auth {
        private boolean registrationEnabled = true;

        public boolean isRegistrationEnabled() {
            return registrationEnabled;
        }

        public void setRegistrationEnabled(boolean registrationEnabled) {
            this.registrationEnabled = registrationEnabled;
        }
    }
}

