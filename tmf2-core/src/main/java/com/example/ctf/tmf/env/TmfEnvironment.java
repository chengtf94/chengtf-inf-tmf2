package com.example.ctf.tmf.env;

import com.example.ctf.tmf.exception.TMFRuntimeException;
import com.example.ctf.tmf.function.model.ExtensionAnnotationParser;
import com.example.ctf.tmf.function.model.impl.AbilityExtensionAnnotationParser;
import com.example.ctf.tmf.function.model.impl.BusinessExtensionAnnotationParser;
import com.example.ctf.tmf.utils.PropertiesUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 16:12
 */
@Slf4j
@Getter
public class TmfEnvironment {

    private static TmfEnvironment INSTANCE;

    private TmfEnvProperties envProperties = TmfEnvProperties.getInstance();

    private static Properties properties = new Properties();

    private String systemName;

    private String[] customPluginDirs;

    private String pluginFileName;

    private boolean tmfFilterTemplateAndExtension = false;

    private boolean bizConfigOptimize = false;

    private String ipAddress;

    private String pomFileName = "pom.properties";

    public static TmfEnvironment getInstance() {
        if (INSTANCE == null) {
            synchronized (TmfEnvironment.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TmfEnvironment();
                    INSTANCE.init();
                }
            }
        }
        return INSTANCE;

    }

    private void init() {
        InputStream inputStream = null;
        try {
            inputStream = TmfEnvironment.class.getResourceAsStream("/application.properties");
            if (null != inputStream) {
                properties = new Properties();
                properties.load(inputStream);
            }

            if (StringUtils.isNotBlank(System.getProperty("project.name"))) {
                systemName = System.getProperty("project.name");
            } else if (StringUtils.isNotBlank(properties.getProperty("project.name"))) {
                systemName = properties.getProperty("project.name");
            }

            if (StringUtils.isNotBlank(properties.getProperty("tmf.plugin.dirs"))) {
                customPluginDirs = properties.getProperty("tmf.plugin.dirs").split(",");
            }

            initCustomExtensionAnnotationParsers();

            if (StringUtils.isBlank(systemName)) {
                throw new TMFRuntimeException();
            }

            if (StringUtils.isNotBlank(properties.getProperty("tmf.plugin.file"))) {
                pluginFileName = properties.getProperty("tmf.plugin.file");
            }

            String flag = System.getProperty("tmf.tmfFilterTemplateAndExtension");
            if (StringUtils.isNotBlank(flag)) {
                tmfFilterTemplateAndExtension = Boolean.parseBoolean(flag);
            }

            String flag2 = System.getProperty("tmf.bz.config.optimize");
            if (StringUtils.isNotBlank(flag)) {
                bizConfigOptimize = Boolean.parseBoolean(flag2);
            }

            ipAddress = getCurrentIp();

        } catch (Exception e) {
            // ignore
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private String getCurrentIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private void initCustomExtensionAnnotationParsers() {
        if (null != envProperties.getExtensionAnnotationParsers()) {
            return;
        }
        envProperties.setExtensionAnnotationParsers(new ArrayList<>());
        List<Properties> properties = PropertiesUtils.loadAllSpecificProperties("tmf.properties");
        for (Properties property : properties) {
            if (StringUtils.isBlank(property.getProperty("tmf.parse.annotation.extension"))) {
                continue;
            }
            String[] values = property.getProperty("tmf.parse.annotation.extension").split(",");
            for (String value : values) {
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                try {
                    Class<? extends ExtensionAnnotationParser> aClass = (Class<? extends ExtensionAnnotationParser>) Class.forName(value);
                    envProperties.getExtensionAnnotationParsers().add(aClass.newInstance());
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }

        envProperties.getExtensionAnnotationParsers().add(new AbilityExtensionAnnotationParser());
        envProperties.getExtensionAnnotationParsers().add(new BusinessExtensionAnnotationParser());

    }


}
