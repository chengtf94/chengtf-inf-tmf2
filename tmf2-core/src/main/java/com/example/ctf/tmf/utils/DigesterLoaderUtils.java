package com.example.ctf.tmf.utils;

import com.example.ctf.tmf.exception.TMFRuntimeException;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 16:48
 */
public class DigesterLoaderUtils {

    private static ClassLoader DEFAULT_CLASSLOADER = DigesterLoaderUtils.class.getClassLoader();

    public static DigesterLoader newLoader(RulesModule... rulesModules) {
        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(getClassLoader(oldClassLoader));
            return DigesterLoader.newLoader(rulesModules);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }


    /**
     * @see com.example.ctf.tmf.function.config.BusinessManagement
     */
    private static ClassLoader getClassLoader(ClassLoader oldClassLoader) {
        if (null == oldClassLoader) {
            return DEFAULT_CLASSLOADER;
        }
        if (oldClassLoader == DEFAULT_CLASSLOADER) {
            return oldClassLoader;
        }
        try {
            oldClassLoader.loadClass("com.example.ctf.tmf.function.config.BusinessManagement");
        } catch (Throwable t) {
            return DEFAULT_CLASSLOADER;
        }
        return oldClassLoader;
    }


    public static Digester newDigester(DigesterLoader loader) {
        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(getClassLoader(oldClassLoader));
            Digester digester = loader.newDigester();
            digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
            digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            return digester;
        } catch (Throwable t) {
            throw new TMFRuntimeException(t);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }
}
