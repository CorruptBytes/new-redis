package com.example.util;

import io.netty.util.internal.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redis-style configuration utility.
 *
 * - Load once, read many times
 * - Thread-safe
 * - Support reload
 */
public final class PropertiesUtil {

    private static final String CONF_PATH = System.getenv().getOrDefault("REDIS_CONFIG_FILE", "redis_conf.properties");

    /**
     * JVM-level cached properties
     */
    private static volatile Properties PROPS;

    private PropertiesUtil() {
    }

    /* ===================== Core ===================== */

    public static Properties getProParams() {
        if (PROPS == null) {
            synchronized (PropertiesUtil.class) {
                if (PROPS == null) {
                    PROPS = load(CONF_PATH);
                }
            }
        }
        return PROPS;
    }

    /**
     * Reload configuration at runtime (CONFIG RELOAD)
     */
    public static void reload() {
        synchronized (PropertiesUtil.class) {
            PROPS = load(CONF_PATH);
        }
    }

    private static Properties load(String path) {
        Properties props = new Properties();
        try {
            // 首先尝试从当前工作目录中加载配置文件
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                }
            } else {
                // 如果当前工作目录中不存在，则尝试从类路径中加载
                try (InputStream is = PropertiesUtil.class.getResourceAsStream(path)) {
                    if (is != null) {
                        props.load(is);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file: " + path, e);
        }
        return props;
    }

    /* ===================== Network ===================== */

    public static String getNodeAddress() {
        String v = getProParams().getProperty("ip");
        return StringUtil.isNullOrEmpty(v) ? "127.0.0.1" : v.trim();
    }

    public static int getNodePort() {
        try {
            int port = Integer.parseInt(getProParams().getProperty("port"));
            return (port > 0 && port <= 65535) ? port : 6379;
        } catch (Exception e) {
            return 6379;
        }
    }

    public static boolean getTcpKeepAlive() {
        return "yes".equalsIgnoreCase(getProParams().getProperty("tcp_keepalive"));
    }

    /* ===================== Thread Model ===================== */

    /**
     * Accept (boss) threads
     */
    public static int getBossThreads() {
        return getInt("server.boss.threads", 1);
    }

    /**
     * IO / selector threads
     */
    public static int getIoThreads() {
        return getInt(
                "server.io.threads",
                Runtime.getRuntime().availableProcessors() * 2
        );
    }

    /**
     * Command execution threads
     */
    public static int getCommandThreads() {
        return getInt("server.command.threads", 1);
    }

    /* ===================== Persistence ===================== */

    public static boolean getAppendOnly() {
        return "yes".equalsIgnoreCase(getProParams().getProperty("appendonly"));
    }

    public static String getAofPath() {
        String v = getProParams().getProperty("aof_data_dir");
        return StringUtil.isNullOrEmpty(v) ? "./aof_data_dir/" : v.trim();
    }

    /* ===================== Memory ===================== */

    public static long getMaxMemory() {
        try {
            return parseMemory(getProParams().getProperty("maxmemory"));
        } catch (Exception e) {
            return -1L;
        }
    }

    private static long parseMemory(String valueStr) {
        if (StringUtil.isNullOrEmpty(valueStr)) {
            return -1L;
        }

        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]*)");
        Matcher matcher = pattern.matcher(valueStr.trim());
        if (!matcher.matches()) {
            return -1L;
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        return switch (unit) {
            case "", "B" -> value;
            case "K", "KB" -> value * 1024;
            case "M", "MB" -> value * 1024 * 1024;
            case "G", "GB" -> value * 1024 * 1024 * 1024;
            default -> -1L;
        };
    }

    /* ===================== Server Cron ===================== */

    /**
     * serverCron frequency (Hz)
     */
    public static int getHz() {
        return getInt("hz", 10);
    }

    /**
     * activeExpireCycle max execution time per run (ms)
     */
    public static int getActiveExpireMaxTimeMs() {
        return getInt("active-expire-max-time", 25);
    }

    /* ===================== Replication ===================== */

    /**
     * Get master host for replication
     */
    public static String getMasterHost() {
        return getProParams().getProperty("slaveof");
    }

    /**
     * Get master port for replication
     */
    public static int getMasterPort() {
        try {
            String slaveof = getMasterHost();
            if (slaveof != null) {
                String[] parts = slaveof.split(" ");
                if (parts.length == 2) {
                    return Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return 6379;
    }

    /**
     * Get master host address for replication
     */
    public static String getMasterHostAddress() {
        try {
            String slaveof = getMasterHost();
            if (slaveof != null) {
                String[] parts = slaveof.split(" ");
                if (parts.length == 2) {
                    return parts[0];
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }

    /**
     * Check if slaveof is configured
     */
    public static boolean isSlaveofConfigured() {
        return getMasterHostAddress() != null;
    }

    /* ===================== Helpers ===================== */

    private static int getInt(String key, int defaultValue) {
        try {
            int v = Integer.parseInt(getProParams().getProperty(key));
            return v > 0 ? v : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /* ===================== RDB Persistence ===================== */

    public static String getRdbFile() {
        String v = getProParams().getProperty("rdb_file");
        return StringUtil.isNullOrEmpty(v) ? "./dump.rdb" : v.trim();
    }

}
