package org.h3x_licensing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.url")
public class AppUrlProperties {

    private String publicBase = "http://localhost:8080";

    public String getPublicBase() {
        return publicBase;
    }

    public void setPublicBase(String publicBase) {
        this.publicBase = publicBase;
    }
}

