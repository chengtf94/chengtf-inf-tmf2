package com.example.ctf.tmf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 16:24
 */
public class PropertiesUtils {

    public static List<Properties> loadAllSpecificProperties (String resourcePath) {
        List<Properties> propertiesList = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                InputStream inputStream = resource.openStream();
                Properties properties = new Properties();
                properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                propertiesList.add(properties);
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return propertiesList;
    }



}
