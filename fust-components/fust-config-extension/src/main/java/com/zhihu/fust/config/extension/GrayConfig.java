package com.zhihu.fust.config.extension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * gray-config
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrayConfig implements ConfigPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(GrayConfig.class);
    /**
     * 全部灰度配置
     */
    private Map<String, GrayItem> grayItems;
    /**
     * 日志计数器
     */
    @JsonIgnore
    private int logCounter = 0;

    /**
     * 最大日志记录量
     */
    private static final int MAX_LOG_NUM = 1000;

    /**
     * 获取灰度配置
     *
     * @param grayName 灰度名称
     * @return 是否命中灰度比例
     */
    public GrayItem getGrayItem(String grayName) {
        GrayItem grayItem = grayItems.get(grayName);
        if (Objects.isNull(grayItem)) {
            log.error("Apollo|GrayConfig|grayName not configured\ngrayName: {}", grayName);
            grayItem = new GrayItem();
            logCounter++;
        } else if (logCounter != 0) {
            logCounter = 0;
        }
        return grayItem;
    }

    public Map<String, GrayItem> getGrayItems() {
        return grayItems;
    }

    public void setGrayItems(
            Map<String, GrayItem> grayItems) {
        this.grayItems = grayItems;
    }

    public int getLogCounter() {
        return logCounter;
    }

    public void setLogCounter(int logCounter) {
        this.logCounter = logCounter;
    }

    @Override
    public void init() {
        logCounter = 0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GrayItem {
        /**
         * 当前灰度比例
         */
        private Integer currentRate;
        /**
         * （可选）最大灰度比例，默认 1000
         */
        private Integer maxRate;
        /**
         * 白名单列表
         */
        private List<String> whiteList;
        /**
         * 黑名单列表
         */
        private List<String> blackList;

        public GrayItem() {
            this.currentRate = 0;
            this.maxRate = 1;
            this.whiteList = Collections.emptyList();
            this.blackList = Collections.emptyList();
        }

        @JsonCreator
        public GrayItem(@JsonProperty("currentRate") Integer currentRate,
                        @JsonProperty("maxRate") Integer maxRate,
                        @JsonProperty("whiteList") List<String> whiteList,
                        @JsonProperty("blackList") List<String> blackList) {
            this.currentRate = Optional.ofNullable(currentRate).orElse(0);
            this.maxRate = Optional.ofNullable(maxRate).orElse(1000);
            this.whiteList = Optional.ofNullable(whiteList).orElse(Collections.emptyList());
            this.blackList = Optional.ofNullable(blackList).orElse(Collections.emptyList());
        }

        public Integer getCurrentRate() {
            return currentRate;
        }

        public void setCurrentRate(Integer currentRate) {
            this.currentRate = currentRate;
        }

        public Integer getMaxRate() {
            return maxRate;
        }

        public void setMaxRate(Integer maxRate) {
            this.maxRate = maxRate;
        }

        public List<String> getWhiteList() {
            return whiteList;
        }

        public void setWhiteList(List<String> whiteList) {
            this.whiteList = whiteList;
        }

        public List<String> getBlackList() {
            return blackList;
        }

        public void setBlackList(List<String> blackList) {
            this.blackList = blackList;
        }

        /**
         * 是否命中灰度（包含黑、白名单判断）
         *
         * @param id 灰度 id
         * @return 是否命中灰度
         */
        public boolean hit(long id) {
            if (hitWhite(id)) {
                return true;
            }
            if (hitBlack(id)) {
                return false;
            }
            return hitRate(id);
        }

        /**
         * 是否命中灰度比例
         *
         * @param id 灰度 id
         * @return 是否命中灰度比例
         */
        public boolean hitRate(long id) {
            if (currentRate >= maxRate) {
                return true;
            }
            return id % maxRate < currentRate;
        }

        /**
         * 是否是白名单用户
         *
         * @param id 灰度 id
         * @return 是否是白名单用户
         */
        public boolean hitWhite(long id) {
            return whiteList.contains(String.valueOf(id));
        }

        /**
         * 是否是黑名单用户
         *
         * @param id 灰度 id
         * @return 是否是黑名单用户
         */
        public boolean hitBlack(long id) {
            return blackList.contains(String.valueOf(id));
        }

        /**
         * 是否开启开关（灰度比例大于 0 视为开启）
         *
         * @return 是否开启开关
         */
        public boolean isOpen() {
            return currentRate > 0;
        }
    }
}
