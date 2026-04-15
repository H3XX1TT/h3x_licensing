package org.h3x_licensing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class H3xLicensingApplication {

    public static void main(String[] args) {
        SpringApplication.run(H3xLicensingApplication.class, args);
    }

}
