package org.example.simple.business.api;

import org.example.simple.business.ServiceConfiguration;
import com.zhihu.fust.telemetry.sdk.TelemetryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Import(ServiceConfiguration.class)
public class ApiMain {
    public static void main(String[] args) {
        TelemetryInitializer.init();
        SpringApplication application = new SpringApplication(ApiMain.class);
        application.setAdditionalProfiles("api");
        application.run(args);
    }
}
