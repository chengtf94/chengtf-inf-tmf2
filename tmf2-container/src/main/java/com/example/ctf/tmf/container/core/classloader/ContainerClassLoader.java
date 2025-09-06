package com.example.ctf.tmf.container.core.classloader;

import com.example.ctf.tmf.container.core.ContainerSharedClassCache;
import com.example.ctf.tmf.container.manage.ContainerManager;
import com.example.ctf.tmf.container.utils.JarFileUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 18:27
 */
public class ContainerClassLoader extends URLClassLoader {


    private String name;
    private Map<String, String> hotPatchClassMap;

    private static ContainerSharedClassCache containerSharedClassCache;

    private final boolean isBizContainerLoader;

    private ThreadLocal<Boolean> ignoreParentResources = ThreadLocal.withInitial(() -> false);

    public ContainerClassLoader(URL[] urls,
                                ClassLoader parentClassLoader,
                                Map<String, String> hotPatchClassMap,
                                String name) {
        super(urls, parentClassLoader);
        this.name = name;
        this.hotPatchClassMap = hotPatchClassMap;
        this.isBizContainerLoader = ContainerManager.getBizContainerNames().contains(name);
//        List<String> collect = Arrays.stream(urls)
//                .filter(url -> url.getPath().endsWith("jar"))
//                .map(URL::getPath).sorted()
//                .collect(Collectors.toList());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        if (hotPatchClassMap.get(name) != null) {
           try {
               byte[] data = JarFileUtils.getClassBytesFromJarFile(hotPatchClassMap.get(name), name);
               return defineClass(data, 0, data.length);
           } catch (IOException e) {
               throw new ClassNotFoundException("findClass error", e);
           }
       }

        if (containerSharedClassCache != null) {
            Class<?> aClass = containerSharedClassCache.get(name);
            if (aClass != null) {
                return aClass;
            }
        }

        return super.findClass(name);
    }


    public void setIgnoreParentResources(boolean ignoreParentResources) {
        this.ignoreParentResources.set(ignoreParentResources);
    }
}
