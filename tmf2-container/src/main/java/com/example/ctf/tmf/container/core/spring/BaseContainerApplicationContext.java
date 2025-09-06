package com.example.ctf.tmf.container.core.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 19:14
 */
public interface BaseContainerApplicationContext {

    void setParentBeanFactory(BeanFactory parentBeanFactory);


    void setPropertyResourceConfigurerClass(Class<?> ropertyResourceConfigurerClass);

    default void waitBeanFactory() throws InterruptedException {

    }
}
