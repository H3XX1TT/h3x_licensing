package org.h3x_licensing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    private String fromAddress = "noreply@h3x.local";
    private String supportAddress = "support@h3x.local";

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getSupportAddress() {
        return supportAddress;
    }

    public void setSupportAddress(String supportAddress) {
        this.supportAddress = supportAddress;
    }
}


