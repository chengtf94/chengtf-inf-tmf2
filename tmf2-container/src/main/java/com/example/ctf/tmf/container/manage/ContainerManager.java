package com.example.ctf.tmf.container.manage;

import com.example.ctf.tmf.container.core.Container;
import com.example.ctf.tmf.container.core.LifeCycleContainer;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.ctf.tmf.container.core.LifeCycleContainer.DYNAMIC_CLASSES_DIR;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 15:33
 */
public class ContainerManager {

    @Getter
    private static boolean enableContainer = false;
    @Getter
    private static boolean enableTenant = false;

    private static String pluginDir = null;

    private static String productDir = null;

    private static final Map<String, String> bizContainers = findBizContainers();

    private static Map<String, String> parentMap = new ConcurrentHashMap<>();


    private final static Map<String, LifeCycleContainer> nameToContainerMap = new ConcurrentHashMap<>();



    public static Map<String, String> getParentMap() {
        if (null == parentMap) {
            parentMap = new ConcurrentHashMap<>();
            getContainerPath();
            getProductPath();
        }
        return parentMap;
    }




    static {

        String flag = System.getProperty("halo.container.enable");
        if (StringUtils.isNotBlank(flag)) {
            enableContainer = Boolean.getBoolean(flag);
        }

        String flag2 = System.getProperty("halo.tenant.enable");
        if (StringUtils.isNotBlank(flag2)) {
            enableTenant = Boolean.getBoolean(flag2);
        }


    }

    @NonNull
    private static Map<String, String> findBizContainers() {
        Map<String, String> bizContainerMap = new HashMap<>();
        String basePath = getBaseDir();
        String relativePath = System.getProperty("halo.container.biz.dirs", "/halo/");
        for (String path : relativePath.split(",")) {
            File file = new File(new File(basePath).getParentFile().getAbsolutePath() + path);
            if (!file.exists() || !file.isDirectory()) {
                continue;
            }
            File[] files = file.listFiles();
            if (files == null) {
                continue;
            }
            for (File subDir : files) {
                if (!subDir.isDirectory()) {
                    continue;
                }
                if (subDir.getName().equals(DYNAMIC_CLASSES_DIR)) {
                    continue;
                }
                bizContainerMap.put(generateContainerName(subDir.getAbsolutePath()), subDir.getAbsolutePath());
            }
        }
        return bizContainerMap;
    }

    public static String generateContainerName(String absolutePath) {
        return StringUtils.substringAfter(absolutePath, File.separator);
    }

    private static List<String> getProductPath() {
        String productDir = getProductDir();
        List<String> paths = doGetContainerPath(productDir);
        addExtraPaths("/product", paths);
        return paths;
    }

    public static List<String> getContainerPath() {
        String pluginDir = getPluginDir();
        List<String> paths = doGetContainerPath(pluginDir);
        addExtraPaths("/app", paths);
        return null;
    }

    private static void addExtraPaths(String subDir, List<String> paths) {
        for (String extraContainer : bizContainers.values()) {
            String extraApps = extraContainer + subDir;
            List<String> extraPaths = doGetContainerPath(extraApps);
            paths.addAll(extraPaths);
            for (String extraPath : extraPaths) {
                parentMap.put(generateContainerName(extraPath), generateContainerName(extraContainer));
            }
        }
    }

    private static List<String> doGetContainerPath(String containerPath) {
        if (StringUtils.isBlank(pluginDir)) {
            return new ArrayList<>();
        }
        File containerDir = new File(containerPath);
        if (!containerDir.isDirectory()) {
            return Collections.emptyList();
        }
        File[] jarDirs = containerDir.listFiles();
        if (null == jarDirs) {
            return Collections.emptyList();
        }
        return Arrays.stream(jarDirs)
                .filter(Objects::nonNull)
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    private static String getPluginDir() {
        if (StringUtils.isNotBlank(pluginDir)) {
            return pluginDir;
        }
        pluginDir = getBaseDir() + File.separatorChar + "app";
        if (pluginDir.startsWith("file:")) {
            pluginDir = pluginDir.replace("file:", "");
        }
        return pluginDir;
    }

    private static String getProductDir() {
        if (StringUtils.isNotBlank(productDir)) {
            return productDir;
        }
        productDir = getBaseDir() + File.separatorChar + "product";
        if (productDir.startsWith("file:")) {
            productDir = productDir.replace("file:", "");
        }
        return productDir;
    }

    private static String getBaseDir() {
        String jarFilePath = ContainerManager.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File jarFile = new File(jarFilePath);
        return jarFile.getParentFile().getParent();
    }


    public static LifeCycleContainer retrieveContainerByName(String parentContainerName) {
        return nameToContainerMap.get(parentContainerName);
    }

    public static Container createContainer(LifeCycleContainerFactory factory) throws Exception {
        if (!enableContainer) {
            return null;
        }
        long start = System.currentTimeMillis();
        return factory.create();
    }


    public static Set<String> getBizContainerNames() {
        return Sets.newHashSet(bizContainers.keySet());
    }

    private static boolean delayRefreshSpring = false;

    static {
        String flag = System.getProperty("halo.container.delay.refresh.spring");
        if (StringUtils.isNotBlank(flag)) {
            delayRefreshSpring = Boolean.getBoolean(flag);
        }
    }

    public static boolean isDelayRefreshSpring() {
        return delayRefreshSpring;
    }
}
