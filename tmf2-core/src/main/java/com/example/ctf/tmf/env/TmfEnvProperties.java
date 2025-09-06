package com.example.ctf.tmf.env;

import com.example.ctf.tmf.function.model.ExtensionAnnotationParser;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 16:19
 */
@Data
public class TmfEnvProperties {

    private static TmfEnvProperties INSTANCE = new TmfEnvProperties();

    public static TmfEnvProperties getInstance() {
        return INSTANCE;
    }

    private List<ExtensionAnnotationParser> extensionAnnotationParsers;

}
