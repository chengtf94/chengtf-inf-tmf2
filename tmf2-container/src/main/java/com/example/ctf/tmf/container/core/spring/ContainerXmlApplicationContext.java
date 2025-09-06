package com.example.ctf.tmf.container.core.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 19:13
 */
public class ContainerXmlApplicationContext extends AbstractXmlApplicationContext implements BaseContainerApplicationContext {

    private Resource[] resources;

    public ContainerXmlApplicationContext(Resource[] resource, boolean refresh) {
        super(null);
        this.resources = resources;
        if (refresh) {
            super.refresh();
        }

    }

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
