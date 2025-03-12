package com.zhihu.fust.example.api;

import com.zhihu.fust.example.business.ServiceConfiguration;
import com.zhihu.fust.telemetry.sdk.TelemetryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

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
