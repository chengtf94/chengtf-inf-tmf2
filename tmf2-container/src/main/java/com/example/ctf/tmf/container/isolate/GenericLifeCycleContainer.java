package com.example.ctf.tmf.container.isolate;

import com.example.ctf.tmf.container.core.ContainerType;
import com.example.ctf.tmf.container.core.LifeCycleContainer;
import org.springframework.context.ApplicationContext;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:54
 */
public class GenericLifeCycleContainer extends LifeCycleContainer {

    public GenericLifeCycleContainer(String name, String path, ContainerType type, ClassLoader parentClassLoader, ApplicationContext parentApplicationContext, boolean fromReload) {
        super(name, path, type, parentClassLoader, parentApplicationContext, fromReload);
    }

}
