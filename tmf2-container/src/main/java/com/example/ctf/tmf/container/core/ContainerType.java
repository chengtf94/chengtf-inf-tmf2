package com.example.ctf.tmf.container.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 17:18
 */
@Getter
@AllArgsConstructor
public enum ContainerType {

    APP(40),
    ;

    private final int priority;
}
