package com.example.ctf.tmf.plugin;

import com.example.ctf.tmf.boot.util.FilePathUtils;
import com.example.ctf.tmf.container.AppContainerListener;
import com.example.ctf.tmf.container.ContainerUtils;
import com.example.ctf.tmf.container.ProductContainerListener;
import com.example.ctf.tmf.container.core.Container;
import com.example.ctf.tmf.container.core.ContainerType;
import com.example.ctf.tmf.container.isolate.TenantContainerFactory;
import com.example.ctf.tmf.container.manage.ContainerManager;
import com.example.ctf.tmf.container.manage.LifeCycleContainerFactory;
import com.example.ctf.tmf.env.TmfEnvironment;
import com.example.ctf.tmf.exception.TMFRuntimeException;
import com.example.ctf.tmf.plugin.config.PluginConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @description: 插件管理器
 * @author: 成腾飞
 * @date: 2025/9/6 15:17
 */
@Slf4j
public class PluginManager {

    private static volatile  PluginManager INSTANCE = null;

    private static volatile  boolean initialized = false;

    @Getter
    private static volatile  boolean finished = false;

    private Set<String> whitePluginJars = new CopyOnWriteArraySet<>();
    private Set<String> blackPluginJars = new CopyOnWriteArraySet<>();

    private Map<String, CountDownLatch> pluginCountDownLatchMap = new ConcurrentHashMap<>();


    /**
     * 获取插件管理器单例
     */
    public static PluginManager getInstance() {
        PluginManager pluginManager = INSTANCE;
        if (pluginManager == null) {
            synchronized (PluginManager.class) {
                pluginManager = INSTANCE;
                if (pluginManager == null) {
                    pluginManager = new PluginManager();
                    try {
                        pluginManager.init();
                    } catch (Throwable t) {
                        throw new TMFRuntimeException(t);
                    }
                }
            }
        }
        return pluginManager;
    }

    /**
     * 初始化
     */
    private void init() throws Throwable {
        synchronized (this) {
            while (initialized && !finished) {
                wait(1000);
            }
            if (initialized && finished) {
                return;
            }
            if (initialized) {
                return;
            }
            initialized = true;
            log.info("[TMF2] PluginManager int, loading plugins...");

            initContainer();

//            initPluginExtendInfo();
//
//





        }

    }

    /**
     * 初始化容器
     */
    private void initContainer() throws Throwable {
        initAppContainer();
        initProductContainer();
    }

    /**
     * 初始化ProductContainer
     */
    private void initProductContainer() {

    }

    /**
     * 初始化APP容器
     */
    private void initAppContainer () throws Exception {
        if (!ContainerManager.isEnableContainer()) {
            return;
        }
        List<String> paths = ContainerManager.getContainerPath();
        for (String path : paths) {
            initAppContainer0(path);
        }
    }

    private void initAppContainer0(String path) throws Exception {
        FilePathUtils.cleanClassByDirectory(FilePathUtils.getContainerFilePath(path));
        File dir = new File(path);
        File[] files = dir.listFiles((file, name) -> name.toLowerCase().endsWith(".jar"));
        if (files == null && files.length <= 0) {
            return;
        }

        List<PluginConfig> pluginConfigs = scanPluginConfigs(files);
        if (CollectionUtils.isEmpty(pluginConfigs)) {
            return;
        }

        List<String> pluginKeys = pluginConfigs.stream()
                .map(PluginConfig::getKey)
                .collect(Collectors.toList());

        String containerName = ContainerUtils.generateContainerName(path);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (String pluginKey : pluginKeys) {
            pluginCountDownLatchMap.put(pluginKey, countDownLatch);
        }
        LifeCycleContainerFactory factory = TenantContainerFactory.getInstance()
                .setName(containerName)
                .setPath(path)
                .setType(ContainerType.APP)
                .setParentApplicationContext(ContainerUtils.getParentContext(containerName))
                .setParentClassLoader(ContainerUtils.getParentClassLoader(containerName))
                .addLifeCycleListener(new AppContainerListener(pluginCountDownLatchMap))
                .addLifeCycleListener(new ProductContainerListener());

        Container container = ContainerManager.createContainer(factory);

//        ContainerUtils.setAppPluginKey(container, pluginKeys.toArray(new String[0]));

    }

    private final List<PluginConfig> eagerPluginConfigs = new CopyOnWriteArrayList<>();

    @NonNull
    private List<PluginConfig> scanPluginConfigs(File[] files) throws Exception {
        List<PluginConfig> pluginConfigs = new ArrayList<>();
        for (File file : files) {
            JarFile jarFile = new JarFile(file);
            PluginConfig pluginConfig = parsePluginConfigFromJarFile(jarFile);
            if (null != pluginConfig) {
                pluginConfigs.add(pluginConfig);
                eagerPluginConfigs.add(pluginConfig);
            }
        }
        return pluginConfigs;
    }

    private PluginConfig parsePluginConfigFromJarFile(JarFile jarFile) throws Exception {
        String pluginFile = TmfEnvironment.getInstance().getPluginFileName();
        ZipEntry zipEntry = jarFile.getEntry(pluginFile);
        if (null == zipEntry) {
            pluginFile = "tmf-plugin.xml";
            zipEntry = jarFile.getEntry(pluginFile);
            if (null == zipEntry) {
                return null;
            }
        }

        log.info(">>>[TMF] found" + TmfEnvironment.getInstance().getPluginFileName() + " in " + jarFile.getName());
        InputStream inputStream = jarFile.getInputStream(zipEntry);
        if (null == inputStream) {
            return null;
        }

        PluginConfig pluginConfig = PluginConfig.parse(inputStream);
        if (null == pluginConfig) {
            log.error("parse plugin-xml error, jarFileName={}", jarFile.getName());
            return null;
        }

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (!jarEntryName.startsWith("META-INF") || !jarEntryName.endsWith(TmfEnvironment.getInstance().getPomFileName())) {
                continue;
            }
            Properties properties = new Properties();
            properties.load(jarFile.getInputStream(jarEntry));
            pluginConfig.setGroupId(properties.getProperty("groupId"));
            pluginConfig.setArtifactId(properties.getProperty("artifactId"));
            pluginConfig.setJarVersion(properties.getProperty("version"));
            if (StringUtils.isNoneBlank(pluginConfig.getGroupId(), pluginConfig.getArtifactId())) {
                if (!isNeedLoad(pluginConfig.getGroupId() + "@" + pluginConfig.getArtifactId())) {
                    return null;
                }
            }
        }

        return pluginConfig;
    }

    private boolean isNeedLoad(String key) {
        if (CollectionUtils.isNotEmpty(whitePluginJars)) {
            return whitePluginJars.contains(key);
        } else {
            return !blackPluginJars.contains(key);
        }
    }


}
