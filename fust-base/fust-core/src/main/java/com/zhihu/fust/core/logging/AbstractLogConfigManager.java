package com.zhihu.fust.core.logging;

import com.zhihu.fust.commons.io.FileUtils;
import com.zhihu.fust.commons.io.IOUtils;
import com.zhihu.fust.commons.lang.PropertyUtils;
import com.zhihu.fust.commons.lang.SpiServiceLoader;
import com.zhihu.fust.commons.lang.StringUtils;
import com.zhihu.fust.core.env.Env;
import com.zhihu.fust.core.logging.spi.LoggingConfigManager;
import com.zhihu.fust.core.logging.spi.LoggingSystemInfo;
import com.zhihu.fust.provider.EnvironmentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AbstractLogConfigManager implements LoggingConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLogConfigManager.class);
    protected final LoggingSystemInfo loggingSystemInfo;
    private final EnvironmentProvider env;
    private static final Logger log = LoggerFactory.getLogger(AbstractLogConfigManager.class);
    protected static final Map<String, String> CACHE = new HashMap<>();
    protected static final Set<String> DEFAULT_LOG_CONFIG_KEYS = new HashSet<>(Arrays.asList(
            LogConstants.CONSOLE_LEVEL_KEY, LogConstants.LOG_CFG_DIR_KEY, LogConstants.FILE_LEVEL_KEY,
            LogConstants.FILE_LOG_DIR_KEY, LogConstants.FILE_BASE_SIZE_KEY, LogConstants.FILE_MAX_SIZE_KEY,
            LogConstants.ROOT_LEVEL_KEY
    ));
    protected String templateContent;
    protected LogLevel rootLevel;
    protected File fileLogDir;
    protected Map<String, String> currentParams;
    protected File logCfgDir;
    protected String logCfgFilePath;

    protected final LogProperties logProperties;
    protected final Map<String, String> logProps = new HashMap<>();
    protected final Map<String, String> envLogProps = new HashMap<>();

    protected AbstractLogConfigManager() {
        this.env = Env.getProvider();
        this.loggingSystemInfo = SpiServiceLoader.get(LoggingSystemInfo.class).orElse(null);
        if (this.loggingSystemInfo == null) {
            throw new IllegalStateException("loggingSystemInfo is null");
        }
        this.logProperties = new LogProperties();
    }

    public void initLogConfig() {
        // init system default to logProps
        initLogPropsBySystemDefault();
        // init log properties
        initLogPropsBySystemEnvOrProp();
        initByProperties(logProperties.getProperties());

        String content = loadTemplate();
        if (content.isEmpty()) {
            logger.info("not find template file|{}", loggingSystemInfo.getTemplateFile());
            return;
        }

        // init template content
        templateContent = content;

        // set root level
        String defaultRootLevel = "WARN";
        if (!Env.isProduction()) {
            defaultRootLevel = "INFO";
        }
        rootLevel = parseLogLevel(defaultRootLevel, LogLevel.WARN);

        // log cfg dir
        Set<String> configKeys = new HashSet<>(DEFAULT_LOG_CONFIG_KEYS);
        configKeys.addAll(loggingSystemInfo.getCustomKeys());

        // file log dir
        fileLogDir = tryCreateDir(getLogProp(LogConstants.FILE_LOG_DIR_KEY), Env.getGeneratedDir());
        // update to configs
        logProps.put(LogConstants.FILE_LOG_DIR_KEY, fileLogDir.getAbsolutePath());
        // log cfg dir
        logCfgDir = tryCreateDir(getLogProp(LogConstants.LOG_CFG_DIR_KEY), Env.getGeneratedDir());

        currentParams = toParams(configKeys, this::getLogProp);
        logCfgFilePath = saveToConfigFile(currentParams, getLoggers());
        // set spring logging config
        System.setProperty(LogConstants.ROOT_LEVEL_PROPERTY_NAME, rootLevel.name());
        if (!logCfgFilePath.isEmpty()) {
            System.setProperty(LogConstants.CONFIG_PROPERTY, logCfgFilePath);
        }
    }

    protected void initLogPropsBySystemDefault() {
        for (String key : loggingSystemInfo.getCustomKeys()) {
            String value = loggingSystemInfo.getDefaultValue(key);
            if (StringUtils.isNotEmpty(value)) {
                logProps.put(key, value);
            }
        }
    }

    protected void initLogPropsBySystemEnvOrProp() {
        for (String key : DEFAULT_LOG_CONFIG_KEYS) {
            // 环境变量或属性设置，优先级更高
            String value = PropertyUtils.getProperty(key);
            if (value != null) {
                envLogProps.put(key, value);
            }
        }
    }

    protected void initByProperties(Properties properties) {
        for (String key : DEFAULT_LOG_CONFIG_KEYS) {
            // 从配置文件中获取
            String value = properties.getProperty(key);
            if (StringUtils.isNotEmpty(value)) {
                logProps.put(key, value);
            }
        }
    }

    protected String getLogProp(String key) {
        if (envLogProps.containsKey(key)) {
            return envLogProps.get(key);
        }
        return logProps.getOrDefault(key, "");
    }

    public String getLogCfgFilePath() {
        return logCfgFilePath;
    }

    /**
     * 获取初始 Root Log 级别
     */
    @Override
    public LogLevel getInitRootLevel() {
        LogLevel initLevel = LogLevel.INFO;
        if (env.isProduction()) {
            initLevel = LogLevel.WARN;
        }
        String level = System.getProperty(LogConstants.ROOT_LEVEL_PROPERTY_NAME);
        if (level == null || level.isEmpty()) {
            level = System.getenv(LogConstants.ROOT_LEVEL_ENV_NAME);
        }
        return parseLogLevel(level, initLevel);
    }

    @Override
    public LogLevel getRootLevel() {
        return rootLevel;
    }

    /**
     * save to local file system
     */
    protected String saveToConfigFile(Map<String, String> params, String loggers) {
        String content = templateContent;
        for (String name : params.keySet()) {
            String key = String.format("#%s#", name);
            String value = params.getOrDefault(name, "");
            if (!value.isEmpty()) {
                content = content.replace(key, value);
            } else if (name.endsWith(".level")) {
                content = content.replace(key, rootLevel.name());
            }
        }

        // add loggers
        if (!loggers.isEmpty()) {
            content = content.replace(LogConstants.LOGGER_PLACEHOLDER, loggers);
        }
        String configDir = getLogConfigDir();
        File dir = Paths.get(configDir).toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("make log dir failed! path|{}", dir.getAbsolutePath());
            return "";
        }

        String filePath = loggingSystemInfo.getFilePath(configDir);
        try {
            FileUtils.write(new File(filePath), content, StandardCharsets.UTF_8);
            log.info("save cfg file to {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.warn("save log cfg file|{} failed!", filePath);
        }
        return "";
    }

    /**
     * 将配置转换为 Map
     * 对 pattern 和 root.level 基本做特殊处理
     */
    public Map<String, String> toParams(Set<String> keys, UnaryOperator<String> getter) {
        LogLevel defaultLevel = LogLevel.WARN;
        if (env.isDevelop()) {
            defaultLevel = LogLevel.INFO;
        }
        Map<String, String> params = new HashMap<>(16);
        for (String key : keys) {
            String value = getter.apply(key);
            if (key.endsWith(".level")) {
                LogLevel logLevel = parseLogLevel(value, defaultLevel);
                value = logLevel.name();
            }

            params.put(key, value);
        }

        String filePath = Optional.ofNullable(env.getServiceName()).orElse("default").toLowerCase();
        if (isUnitTest()) {
            filePath = "unit-test";
        }
        // update file log dir
        if (params.containsKey(LogConstants.FILE_LOG_DIR_KEY)) {
            fileLogDir = tryCreateDir(params.get(LogConstants.FILE_LOG_DIR_KEY), "");
        }

        // use unit-name as log name
        if (fileLogDir.exists()) {
            filePath = Paths.get(fileLogDir.getAbsolutePath(), filePath).toFile().getAbsolutePath();
        }
        log.info("fileLog|{}.log", Paths.get(filePath).toFile().getAbsolutePath());
        params.put("_file_path_", filePath);
        return params;
    }

    protected File tryCreateDir(String path, String defaultPath) {
        if (path != null) {
            path = path.trim();
        }
        if (StringUtils.isEmpty(path)) {
            path = defaultPath;
        }

        File dir = new File(path);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                dir = FileUtils.getTempDirectory();
                log.warn("create origin dir|{} failed, use temp dir|{}", path, dir.getAbsolutePath());
            }
        }
        return dir;
    }

    protected LogLevel parseLogLevel(String level, LogLevel defaultLevel) {
        return Arrays.stream(LogLevel.values())
                .filter(x -> x.name().equalsIgnoreCase(level))
                .findFirst()
                .orElse(defaultLevel);
    }

    protected String loadTemplate() {
        String templateFile = loggingSystemInfo.getTemplateFile();
        String result = CACHE.get(templateFile);
        if (result != null) {
            return result;
        }
        try {
            InputStream stream = loggingSystemInfo.getClass().getClassLoader().getResourceAsStream(
                    templateFile);
            if (stream == null) {
                // for unit test
                URL resource = loggingSystemInfo.getClass().getResource(templateFile);
                if (resource != null) {
                    stream = resource.openStream();
                }
            }
            if (stream != null) {
                result = IOUtils.toString(stream, StandardCharsets.UTF_8);
                CACHE.put(templateFile, result);
                IOUtils.closeQuietly(stream);
                return result;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load template from location [" +
                    templateFile + "]", ex);
        }
        return "";
    }

    protected String getLoggers() {
        Map<String, String> config = logProperties.getLoggers();
        StringBuilder sb = new StringBuilder();
        config.forEach((k, v) -> {
            sb.append(String.format(LogConstants.LOGGER_LEVEL_FMT, k, LogConstants.normalize(v)));
        });
        return sb.toString();
    }

    private String getLogConfigDir() {
        if (logCfgDir.exists()) {
            return logCfgDir.getAbsolutePath();
        }
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "log-cfg");
        if (cacheDir.exists()) {
            return cacheDir.getAbsolutePath();
        }
        if (cacheDir.mkdirs()) {
            log.info("create log config dir|{}", cacheDir.getAbsolutePath());
            return cacheDir.getAbsolutePath();
        }
        throw new IllegalStateException("create log config dir error, dir|" + cacheDir.getAbsolutePath());
    }

    public static boolean isUnitTest() {
        Exception e = new Exception();
        StackTraceElement[] traceList = e.getStackTrace();
        for (StackTraceElement traceElement : traceList) {
            if (traceElement.getClassName().contains("junit")) {
                return true;
            }
        }
        return false;
    }
}
