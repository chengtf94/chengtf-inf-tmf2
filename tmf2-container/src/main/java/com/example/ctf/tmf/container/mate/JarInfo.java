package com.example.ctf.tmf.container.mate;

import com.example.ctf.tmf.container.utils.JarFileUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 18:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JarInfo {

    private String path;
    private String groupId;
    private String artifactId;
    private String version;

    public static JarInfo parseJarInfo(JarFile jarFile) throws IOException {
        JarInfoBuilder builder = JarInfo.builder().path(jarFile.getName());
        List<JarEntry> jarEntries = JarFileUtils.getJarEntries(jarFile, jarEntry -> jarEntry.getName().contains("pom.properties"));
        if (CollectionUtils.isEmpty(jarEntries)) {
            return builder.build();
        }
        InputStream inputStream = jarFile.getInputStream(jarEntries.get(0));
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        return builder
                .groupId(properties.getProperty("groupId"))
                .artifactId(properties.getProperty("artifactId"))
                .version(properties.getProperty("version"))
                .build();
    }

}
