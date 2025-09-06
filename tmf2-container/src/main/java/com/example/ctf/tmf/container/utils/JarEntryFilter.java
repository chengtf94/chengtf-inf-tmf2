package com.example.ctf.tmf.container.utils;

import java.util.jar.JarEntry;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 18:13
 */
public interface JarEntryFilter {

    boolean accept(JarEntry jarEntry);

}
