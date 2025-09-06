package com.example.ctf.tmf.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:36
 */
public class SpringApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }

}
