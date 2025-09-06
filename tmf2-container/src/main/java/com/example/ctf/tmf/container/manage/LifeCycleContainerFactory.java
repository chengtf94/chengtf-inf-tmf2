package com.example.ctf.tmf.container.manage;

import com.example.ctf.tmf.container.core.ContainerType;
import com.example.ctf.tmf.container.core.LifeCycleContainer;
import com.example.ctf.tmf.container.core.Listener;
import org.springframework.context.ApplicationContext;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:16
 */
public abstract class LifeCycleContainerFactory {

    protected String name;
    protected String path;
    protected ContainerType type;

    protected ApplicationContext parentApplicationContext;
    protected ClassLoader parentClassLoader;

    protected List<Listener> listeners = new ArrayList<>();

    protected boolean fromReload = false;

    public LifeCycleContainerFactory setName(String name) {
        this.name = name;
        return this;
    }

    public LifeCycleContainerFactory setPath(String path) {
        this.path = path;
        return this;
    }

    public LifeCycleContainerFactory setType(ContainerType type) {
        this.type = type;
        return this;
    }

    public LifeCycleContainerFactory setParentApplicationContext(ApplicationContext parentApplicationContext) {
        this.parentApplicationContext = parentApplicationContext;
        return this;
    }

    public LifeCycleContainerFactory setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }

    public LifeCycleContainerFactory addLifeCycleListener(Listener listener) {
        if (null != listener) {
            listeners.add(listener);
        }
        return this;
    }

    abstract protected LifeCycleContainer create();

}
