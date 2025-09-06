package com.example.ctf.tmf.plugin.config;

import com.example.ctf.tmf.plugin.config.rule.PluginDigesterRulesModule;
import com.example.ctf.tmf.utils.DigesterLoaderUtils;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @description: 插件配置类
 * @author: 成腾飞
 * @date: 2025/9/6 16:08
 */
@Data
public class PluginConfig {

    private String key;

    private String groupId;
    private String artifactId;
    private String jarVersion;








    private static Object lock = new Object();

    private static final DigesterLoader loader = DigesterLoaderUtils.newLoader(new PluginDigesterRulesModule());

    private static ThreadLocal<Digester> digester = ThreadLocal.withInitial(() -> {
        synchronized (lock) {
            return DigesterLoaderUtils.newDigester(loader);
        }
    });

    public static PluginConfig parse(InputStream inputStream) throws Exception {
        digester.get().clear();
        digester.get().resetRoot();
        return digester.get().parse(inputStream);
    }
}
