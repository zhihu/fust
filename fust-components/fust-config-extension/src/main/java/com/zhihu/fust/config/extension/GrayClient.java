package com.zhihu.fust.config.extension;

import com.zhihu.fust.provider.ConfigCustomProvider;

/**
 * gray-config
 *
 * @author wanghan
 * @since 2021/9/29 2:05 下午
 */
public final class GrayClient {
    /**
     * 灰度配置 namespace
     */
    private static String grayNamespace = "gray-config";
    /**
     * configClient
     */
    private static ConfigClient configClient;

    private static ConfigClient getConfigClient() {
        if (configClient == null) {
            configClient = new DefaultConfigClient();
        }
        return configClient;
    }

    private static String getGrayNamespace() {
        if (grayNamespace == null) {
            ConfigCustomProvider provider = getConfigClient().getConfigService().getProvider();
            grayNamespace = provider.grayConfigFileName();
        }
        return grayNamespace;
    }

    private GrayClient() {
        throw new IllegalStateException("Client class");
    }

    /**
     * 是否命中灰度（包含黑、白名单判断）
     *
     * @param grayName 灰度名称
     * @param id       灰度 id
     * @return 是否命中灰度
     */
    public static boolean hit(String grayName, long id) {
        return getConfig(grayName).hit(id);
    }

    /**
     * 是否命中灰度比例
     *
     * @param grayName 灰度名称
     * @param id       灰度 id
     * @return 是否命中灰度比例
     */
    public static boolean hitRate(String grayName, long id) {
        return getConfig(grayName).hitRate(id);
    }

    /**
     * 是否是白名单用户
     *
     * @param grayName 灰度名称
     * @param id       灰度 id
     * @return 是否是白名单用户
     */
    public static boolean hitWhite(String grayName, long id) {
        return getConfig(grayName).hitWhite(id);
    }

    /**
     * 是否是黑名单用户
     *
     * @param grayName 灰度名称
     * @param id       灰度 id
     * @return 是否是黑名单用户
     */
    public static boolean hitBlack(String grayName, long id) {
        return getConfig(grayName).hitBlack(id);
    }

    /**
     * 是否开启开关（灰度比例大于等于 1 视为开启）
     *
     * @param grayName 灰度名称
     * @return 是否开启开关
     */
    public static boolean isOpen(String grayName) {
        return getConfig(grayName).isOpen();
    }

    /**
     * 获取配置
     *
     * @param grayName 灰度名称
     * @return 返回灰度配置
     */
    public static GrayConfig.GrayItem getConfig(String grayName) {
        String namespace = getGrayNamespace();
        GrayConfig grayConfig = getConfigClient()
                .getConfigByJsonNamespace(GrayConfig.class, namespace);
        return grayConfig.getGrayItem(grayName);
    }
}
