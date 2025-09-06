package com.example.ctf.tmf.container.core.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 19:19
 */
public class ContainerAnnotationConfigApplicationContext
        extends AnnotationConfigApplicationContext
        implements BaseContainerApplicationContext{


    @Override
    public void setParentBeanFactory(BeanFactory parentBeanFactory) {

    }

    @Override
    public void setPropertyResourceConfigurerClass(Class<?> ropertyResourceConfigurerClass) {

    }

    @Override
    public void waitBeanFactory() throws InterruptedException {
        BaseContainerApplicationContext.super.waitBeanFactory();
    }
}
