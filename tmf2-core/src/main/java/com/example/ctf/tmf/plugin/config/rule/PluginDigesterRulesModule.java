package com.example.ctf.tmf.plugin.config.rule;

import org.apache.commons.digester3.xmlrules.FromXmlRulesModule;

/**
 * @description: TODO
 * @author: 成腾飞
 * @date: 2025/9/6 16:56
 */
public class PluginDigesterRulesModule extends FromXmlRulesModule {

    @Override
    protected void loadRules() {
        this.loadXMLRules(PluginDigesterRulesModule.class.getResource("/plugin-digester-rules.xml"));
    }

}
