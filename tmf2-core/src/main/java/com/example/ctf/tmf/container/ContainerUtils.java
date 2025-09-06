package com.example.ctf.tmf.container;

import com.example.ctf.tmf.container.core.LifeCycleContainer;
import com.example.ctf.tmf.container.manage.ContainerManager;
import com.example.ctf.tmf.utils.SpringApplicationContextHolder;
import org.springframework.context.ApplicationContext;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:11
 */
public class ContainerUtils {

    public static String generateContainerName(String path) {
        return ContainerManager.generateContainerName(path);
    }


    public static ApplicationContext getParentContext(String containerName) {
        String parentContainerName = ContainerManager.getParentMap().get(containerName);
        if (parentContainerName == null) {
            return SpringApplicationContextHolder.getContext();
        }
        LifeCycleContainer parent = ContainerManager.retrieveContainerByName(parentContainerName);
        if (parent == null) {
            return SpringApplicationContextHolder.getContext();
        }
        return parent.getSpringApplicationContext();
    }

    public static ClassLoader getParentClassLoader(String containerName) {
        return ContainerManager.class.getClassLoader();
    }
}
