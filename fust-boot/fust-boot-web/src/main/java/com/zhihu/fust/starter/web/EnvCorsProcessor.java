package com.zhihu.fust.starter.web;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;

import com.zhihu.fust.core.env.Env;

/**
 * special process test/dev env cors
 */
public class EnvCorsProcessor extends DefaultCorsProcessor {

    /**
     * request header name -> response header name
     */
    private static final Map<String, String> ECHO_MAP = new HashMap<>();

    static {
        ECHO_MAP.put(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN);
        ECHO_MAP.put(ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_ALLOW_METHODS);
        ECHO_MAP.put(ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_HEADERS);
    }

    private final boolean echoCors;

    public EnvCorsProcessor() {
        echoCors = Env.isDevelop() || Env.isTesting();
    }

    @Override
    public boolean processRequest(@Nullable CorsConfiguration configuration,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {

        if (echoCors) {
            ECHO_MAP.forEach((reqName, respName) -> {
                String value = request.getHeader(reqName);
                if (Objects.nonNull(value)) {
                    response.setHeader(respName, value);
                }
            });
            Boolean allowCredentials = Optional.ofNullable(configuration)
                                               .map(CorsConfiguration::getAllowCredentials)
                                               .orElse(false);

            if (Boolean.TRUE.equals(allowCredentials)) {
                response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(true));
            }

            List<String> exposedHeaders = Optional.ofNullable(configuration)
                                                  .map(CorsConfiguration::getExposedHeaders)
                                                  .orElse(Collections.emptyList());
            if (!CollectionUtils.isEmpty(exposedHeaders)) {
                response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS,
                                   String.join(", ", configuration.getExposedHeaders()));
            }
            return true;
        }
        return super.processRequest(configuration, request, response);
    }
}
