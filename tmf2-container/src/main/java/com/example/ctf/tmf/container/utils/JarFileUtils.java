package com.example.ctf.tmf.container.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 18:12
 */
public class JarFileUtils {

    public static List<JarEntry> getJarEntries(JarFile jarFile, JarEntryFilter jarEntryFilter) {
        List<JarEntry> jarEntries = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (!jarEntry.isDirectory() && jarEntryFilter.accept(jarEntry)) {
                jarEntries.add(jarEntry);
            }
        }
        return jarEntries;
    }

    public static byte[] getClassBytesFromJarFile(String filePath, String className) throws IOException {
        // TODO：成腾飞
        return new byte[0];
    }


}
