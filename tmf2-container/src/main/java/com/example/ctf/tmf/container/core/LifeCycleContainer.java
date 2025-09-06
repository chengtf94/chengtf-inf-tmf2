package com.example.ctf.tmf.container.core;

import com.example.ctf.tmf.container.core.classloader.ContainerClassLoader;
import com.example.ctf.tmf.container.core.spring.ContainerAnnotationConfigApplicationContext;
import com.example.ctf.tmf.container.core.spring.ContainerXmlApplicationContext;
import com.example.ctf.tmf.container.exception.ContainerException;
import com.example.ctf.tmf.container.manage.BizContainerListener;
import com.example.ctf.tmf.container.manage.ContainerManager;
import com.example.ctf.tmf.container.mate.JarInfo;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.example.ctf.tmf.container.mate.JarInfo.parseJarInfo;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 15:49
 */
public abstract class LifeCycleContainer implements Container {

    public static final String DYNAMIC_CLASSES_DIR = "classes";
    public static final String DEFAULT_HOT_PATCH_DIR = "hotpatch";

    private static AtomicInteger idCounter = new AtomicInteger(0);

    protected Integer id;

    protected boolean fromReload = false;
    protected String name;
    protected String path;
    protected ContainerType typ;
    private ClassLoader parentClassLoader;
    private ApplicationContext parentApplicationContext;

    private List<JarInfo> jarInfoList;
    private final boolean isBizContainer;

    private List<Listener> listeners = new ArrayList<>();

    private Map<String, String> hotPatches;

    protected List<Resource> xmlResources;
    private static final String DEFAULT_SPRING_RESOURCE_PATTERN = "class*:spring/spring-*.xml";
    private static String[] springResourcePath = new String[]{DEFAULT_SPRING_RESOURCE_PATTERN};

    @Getter
    @Setter
    private static String[] springAnnotationScanPackages = null;

    /**
     * 最重要的核心！！！
     */
    protected ContainerClassLoader classLoader;
    private AbstractApplicationContext applicationContext;

    public LifeCycleContainer(String name,
                              String path,
                              ContainerType type,
                              ClassLoader parentClassLoader,
                              ApplicationContext parentApplicationContext,
                              boolean fromReload) {

        try {
            this.id = idCounter.incrementAndGet();

            this.fromReload = fromReload;
            this.name = name;
            this.path = path;
            this.typ = type;
            this.parentClassLoader = parentClassLoader;
            this.parentApplicationContext = parentApplicationContext;

            this.jarInfoList = parseJarInfoFromDirectory(path);

            this.isBizContainer = ContainerManager.getBizContainerNames().contains(name);
            if (this.isBizContainer) {
                this.listeners.add(new BizContainerListener());
            }

            /**
             * 最重要的核心实现!!!!
             */
            this.classLoader = createClassLoader();

            /**
             * 最重要的核心实现!!!!
             */
            handleResource(this::loadSelfResource);

        } catch (Throwable e) {
            throw new ContainerException("LifeCycleContainer error, containerName=" + name);
        }

    }

    abstract protected void handleResource(Runnable runnable);

    /**

     */
    private void loadSelfResource() throws ContainerException {
        try {
            createSubSpringContext(this.parentApplicationContext);
        } catch (Throwable t) {
            throw new ContainerException("loadSelfResource error" + this.name);
        }
    }

    /**
     * 最重要的核心实现！！！
     * @return
     */
    private void createSubSpringContext(ApplicationContext parentApplicationContext) throws IOException {

        this.xmlResources = findXmlResourcesInJarFiles();

        this.applicationContext = createApplicationContext(parentApplicationContext);

        applicationContextPostProcess();

        scanPackage();

        if (fromReload || !ContainerManager.isDelayRefreshSpring()) {
            refresh();
        }

    }

    protected void refresh() {
        refreshSpring();
    }

    private void refreshSpring() {
        if (this.applicationContext != null) {
            applicationContext.refresh();
        }
    }

    protected void scanPackage() {
        if (CollectionUtils.isNotEmpty(Lists.newArrayList(springAnnotationScanPackages))
         && this.applicationContext instanceof ContainerAnnotationConfigApplicationContext) {
            try {
                this.classLoader.setIgnoreParentResources(true);
                ((ContainerAnnotationConfigApplicationContext)applicationContext).scan(springAnnotationScanPackages);
            } finally {
                this.classLoader.setIgnoreParentResources(false);
            }
        }
    }

    protected void applicationContextPostProcess() {
        applicationContext.setClassLoader(this.classLoader);
        applicationContext.setParent(parentApplicationContext);
        // 增加自定义后置处理器
    }

    private AbstractApplicationContext createApplicationContext(ApplicationContext parentApplicationContext) {
        if (!this.xmlResources.isEmpty()) {
            ContainerXmlApplicationContext applicationContext = new ContainerXmlApplicationContext(this.xmlResources.toArray(new Resource[0]), false);
            applicationContext.setDisplayName(name);
            return applicationContext;
        } else if (CollectionUtils.isNotEmpty(Lists.newArrayList(springAnnotationScanPackages))) {
            ContainerAnnotationConfigApplicationContext applicationContext = new ContainerAnnotationConfigApplicationContext();
            applicationContext.setDisplayName(name);
            applicationContext.setResourceLoader(new DefaultResourceLoader(this.classLoader));
            return applicationContext;
        }
        return null;
    }

    private List<Resource> findXmlResourcesInJarFiles() throws IOException {
        try {
            this.classLoader.setIgnoreParentResources(true);
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(new DefaultResourceLoader(this.classLoader));
            List<Resource> xmlResources = new ArrayList<>();
            String[] containerSpringResourcePath = springResourcePath;
            if (containerSpringResourcePath != null) {
                for (String resourcePath : containerSpringResourcePath) {
                    Resource[] resources = resolver.getResources(resourcePath);
                    if (null == resources || resources.length == 0) {
                        continue;
                    }
                    for (Resource resource : resources) {
                        URL url = resource.getURL();
                        if (url.getPath().contains(this.path)) {
                            xmlResources.add(resource);
                        }
                    }

                }
            }
            return xmlResources;
        } finally {
            this.classLoader.setIgnoreParentResources(false);
        }
    }

    /**
     * 最重要的核心实现！！！
     * @return
     */
    private ContainerClassLoader createClassLoader() throws Exception {
        URL[] jarUrls = getJarUrls();
        injectToParentLoader(jarUrls);
        return new ContainerClassLoader(jarUrls, this.parentClassLoader, prepareHotPatchClasses(), this.name);
    }

    private Map<String, String> prepareHotPatchClasses() {
        File[] hotPatchJars = scanJarFiles(this.path + File.separator + DEFAULT_HOT_PATCH_DIR);
        if (null == hotPatchJars || hotPatchJars.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> hotPatchClassNameToLocation = new ConcurrentHashMap<>();
        for (File hotPatchJar : hotPatchJars) {
            // jar文件提取jar名称，放在map里
        }
        this.hotPatches = hotPatchClassNameToLocation;
        return this.hotPatches;
    }

    private void injectToParentLoader(URL[] jarUrls) {
        if (!isBizContainer) {
            return;
        }
//        // pandora-boot场景
//        if (this.parentClassLoader.getClass().getName().equals("com.taobao.pandaro.boot.loader.LaunchedURLClassLoader")) {
//            injectToPandoraParentLoader(jarUrls);
//        }
//        // tomcat场景
//        injectToTomcatParentLoader(jarUrls);
    }

    protected URL[] getJarUrls() throws MalformedURLException {
        List<File> jarFiles = this.jarInfoList.stream()
                .map(var -> new File(var.getPath()))
                .sorted(Comparator.comparing(File::getPath))
                .collect(Collectors.toList());
        File dynamicClassesDir = new File(this.name + File.separator + DYNAMIC_CLASSES_DIR);
        URL[] urls;
        int index = 0;
        if (dynamicClassesDir.exists() && dynamicClassesDir.isDirectory()) {
            urls = new URL[jarFiles.size() + 1];
            urls[index++] = dynamicClassesDir.toURI().toURL();
        } else {
            urls = new URL[jarFiles.size()];
        }
        for (File jarFile : jarFiles) {
            urls[index++] = jarFile.toURI().toURL();
        }
        return urls;
    }

    private List<JarInfo> parseJarInfoFromDirectory(String directory) throws IOException {
        List<JarInfo> jarInfoList = new ArrayList<>();
        File[] jarFiles = scanJarFiles(directory);
        if (null == jarFiles) {
            throw new ContainerException("no jar found in " + path);
        }
        for (File jarFile : jarFiles) {
            jarInfoList.add(parseJarInfo(new JarFile(jarFile)));
        }
        if (CollectionUtils.isEmpty(jarInfoList)) {
            throw new ContainerException("no jar found in " + path);
        }

        return jarInfoList;
    }

    private File[] scanJarFiles(String directory) {
        return new File(directory).listFiles((file, name) -> name.toLowerCase().endsWith(".jar"));
    }


    @Override
    public ApplicationContext getSpringApplicationContext() {
        return applicationContext;
    }


    public void addLifeCycleListener(Listener listener) {
        listeners.add(listener);
    }
}
