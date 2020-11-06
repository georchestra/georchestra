package org.georchestra.datafeeder.config;

import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} for the DataFeeder
 * application
 */
public @Data class DataFeederConfigurationProperties {

    private MultipartConfig fileUpload;

    public static @Data class MultipartConfig {
        private String location = null;
        private long maxFileSize = -1;

        public Optional<Long> maxFileSize() {
            return maxFileSize < 0 ? Optional.empty() : Optional.of(maxFileSize);
        }

        public Optional<String> location() {
            return Optional.ofNullable(location);
        }
    }
}
