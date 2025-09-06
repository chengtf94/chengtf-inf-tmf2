package com.example.ctf.tmf.boot.util;

import com.example.ctf.tmf.exception.TMFRuntimeException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 15:57
 */
public class FilePathUtils {

    private static final String CLASS_FILE_PATH_SUFFIX = File.separator + "classes";
    private static final String CLASS_OUTPUT = File.separator + "tmfBootClasses" + File.separator;

    public static void cleanClassByDirectory(String directoryPath) {
        File file = new File(directoryPath);
        if (file.exists() && file.isDirectory()) {
            try {
                FileUtils.cleanDirectory(file);
            } catch (IOException e) {
                throw new TMFRuntimeException(e);
            }
        }
    }

    public static String getContainerFilePath(String filePath) {
        filePath = checkForSpringBoot(filePath);
        File classFilePath = new File(filePath);
        if (classFilePath.isDirectory()) {
            return classFilePath + CLASS_FILE_PATH_SUFFIX + CLASS_OUTPUT;
        } else {
            return filePath.substring(0, filePath.lastIndexOf(File.separator)) + CLASS_FILE_PATH_SUFFIX + CLASS_OUTPUT;
        }
    }

    private static String checkForSpringBoot(String filePath) {
        String result = filePath;
        if (result.startsWith("file:")) {
            result = result.replace("file:", "");
        }
        if (result.startsWith("jar!/")) {
            result = result.replace("!/", "");
        }
        return result;
    }

}
