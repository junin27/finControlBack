// src/main/java/fincontrol/com/fincontrol/config/MetricsConfig.java
package fincontrol.com.fincontrol.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // já existente: commonTags…
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(
            @Value("${spring.application.name:fincontrol-back}") String appName,
            @Value("${spring.profiles.active:default}") String env
    ) {
        return registry -> registry.config()
                .commonTags("application", appName, "environment", env);
    }

    // 🔥 bean que habilita o processamento de @Timed
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
