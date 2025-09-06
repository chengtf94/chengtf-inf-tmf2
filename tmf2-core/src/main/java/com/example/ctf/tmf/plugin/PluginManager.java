package com.example.ctf.tmf.plugin;

import com.example.ctf.tmf.exception.TMFRuntimeException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
    private void initAppContainer() {

    }


}
