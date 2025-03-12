package com.zhihu.fust.boot.lettuce;

import static java.util.stream.Collectors.toList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.zhihu.fust.spring.redis.common.DefaultRedisNodeProperties;
import org.springframework.util.CollectionUtils;

import com.zhihu.fust.commons.io.resource.URLUser;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.config.IConfigService;
import com.zhihu.fust.spring.redis.common.RedisResourceDiscover;

class DefaultRedisResourceDiscover implements RedisResourceDiscover {

    private static final String PRIMARY = "primary";
    private static final String REPLICA = "replica";
    private static final String RWREDIS_FMT = "RES_REDIS_%s_%s_%d";

    @Override
    public List<DefaultRedisNodeProperties> discover(String name) {
        List<DefaultRedisNodeProperties> propertiesList = new ArrayList<>();
        DefaultRedisNodeProperties primary = discoverPrimary(name);
        if (primary != null) {
            propertiesList.add(primary);
        }
        propertiesList.addAll(discoverReplica(name));
        return propertiesList;

    }

    public DefaultRedisNodeProperties discoverPrimary(String name) {
        // master
        List<URL> urls = getResourceUrls(name, PRIMARY);
        if (CollectionUtils.isEmpty(urls)) {
            return null;
        }
        return createNode(urls.get(0), name, true);
    }

    public List<DefaultRedisNodeProperties> discoverReplica(String name) {
        List<URL> urls = getResourceUrls(name, REPLICA);
        if (CollectionUtils.isEmpty(urls)) {
            return Collections.emptyList();
        }
        return urls.stream()
                   .map(url -> createNode(url, name, false))
                   .collect(toList());
    }

    private static List<URL> getResourceUrls(String name, String type) {
        List<URL> urls = getUrlsByEnv(name, type);
        if (CollectionUtils.isEmpty(urls)) {
            urls = getUrlsByEnv(name, type);
        }

        if (CollectionUtils.isEmpty(urls)) {
            urls = getUrlsByApollo(name, type);
        }

        return urls;
    }

    public DefaultRedisNodeProperties createNode(URL url, String name, boolean isPrimary) {
        Optional<URLUser> user = URLUser.of(url);
        DefaultRedisNodeProperties properties = new DefaultRedisNodeProperties();
        properties.setName(name);
        properties.setHost(url.getHost());
        properties.setPort(url.getPort());
        properties.setType(isPrimary ? PRIMARY : REPLICA);
        user.ifPresent(u -> properties.setPassword(u.getPassword()));
        return properties;
    }

    private static List<URL> getUrlsByEnv(String name, String type) {
        return getUrlsByFormat(System::getenv, name, type);
    }

    private static List<URL> getUrlsByApollo(String name, String type) {
        IConfigService configService = SpiServiceLoader.get(IConfigService.class).orElse(null);
        if (configService != null) {
            UnaryOperator<String> apolloGetter = key -> configService.getAppConfig().getProperty(key, "");
            return getUrlsByFormat(apolloGetter, name, type);
        }
        return Collections.emptyList();

    }

    private static List<URL> getUrlsByFormat(UnaryOperator<String> getter, String name, String type) {
        List<URL> urls = new ArrayList<>();
        int index = 0;
        while (true) {
            String key = String.format(RWREDIS_FMT, name, type, index);
            key = key.toUpperCase().replace('-', '_');
            String value = getter.apply(key);

            if (StringUtils.isEmpty(value)) {
                break;
            }

            if (!value.contains("://")) {
                value = String.format("http://%s", value);
            }
            try {
                urls.add(new URL(value));
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            index++;
        }

        return urls;
    }

}
