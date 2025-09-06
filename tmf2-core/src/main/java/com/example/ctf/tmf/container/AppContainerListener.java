package com.example.ctf.tmf.container;

import com.example.ctf.tmf.container.core.Listener;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:44
 */
public class AppContainerListener implements Listener {

    private final Map<String, CountDownLatch> pluginCountDownLatchMap;

    public AppContainerListener() {
        this(null);
    }

    public AppContainerListener(Map<String, CountDownLatch> pluginCountDownLatchMap) {
        this.pluginCountDownLatchMap = pluginCountDownLatchMap;
    }


}
