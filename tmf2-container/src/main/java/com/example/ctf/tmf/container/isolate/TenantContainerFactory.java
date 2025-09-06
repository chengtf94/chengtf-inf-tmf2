package com.example.ctf.tmf.container.isolate;

import com.example.ctf.tmf.container.core.LifeCycleContainer;
import com.example.ctf.tmf.container.core.Listener;
import com.example.ctf.tmf.container.exception.ContainerException;
import com.example.ctf.tmf.container.manage.ContainerManager;
import com.example.ctf.tmf.container.manage.LifeCycleContainerFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:16
 */
public class TenantContainerFactory extends LifeCycleContainerFactory {

    public static TenantContainerFactory getInstance() {
        return new TenantContainerFactory();
    }


    @Override
    protected LifeCycleContainer create() {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(path) || null == type) {
            throw new ContainerException("INVALID");
        }
        LifeCycleContainer container = new GenericLifeCycleContainer(name, path, type, parentClassLoader, parentApplicationContext, fromReload);
        for (Listener listener : listeners) {
            container.addLifeCycleListener(listener);
        }
        return container;
    }
}
