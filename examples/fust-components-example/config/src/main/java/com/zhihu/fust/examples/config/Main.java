package com.zhihu.fust.examples.config;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.config.IConfigProperties;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // set apollo config server address for test only
        System.setProperty("apollo.config-service", "http://localhost:8080");
        
        Env.init();
        ILoggingSystem loggingSystem = ILoggingSystem.get();

        IConfigService configService = SpiServiceLoader.get(IConfigService.class).orElse(null);
        if (configService != null) {
            configService.initialize();
        }

        loggingSystem.initialize();

        if (configService != null) {
            IConfigProperties config = configService.getConfig("application");
            int timeout = config.getIntProperty("timeout", 300);
            logger.info("timeout|{}", timeout);
        }
    }
}
