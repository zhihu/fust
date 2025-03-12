package com.zhihu.fust.boot.starter;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.ILoggingSystem;

public class ApplicationPreparedListener implements ApplicationListener<ApplicationPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        Env.init();
        ILoggingSystem loggingSystem = ILoggingSystem.get();
        SpiServiceLoader.get(IConfigService.class)
                        .ifPresent(IConfigService::initialize);

        // if config service support dynamic logging config, auto config will happen here
        loggingSystem.initialize();

    }
}
