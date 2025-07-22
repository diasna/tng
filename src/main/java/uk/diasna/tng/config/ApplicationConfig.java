package uk.diasna.tng.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class ApplicationConfig {
    @Bean
    public InfoContributor trackingNumberInfoContributor() {
        return new InfoContributor() {
            @Override
            public void contribute(Info.Builder builder) {
                builder.withDetail("service", "Tracking Number Generator")
                       .withDetail("version", "1.0.0")
                       .withDetail("description", "Tracking number generation API");
            }
        };
    }
}
