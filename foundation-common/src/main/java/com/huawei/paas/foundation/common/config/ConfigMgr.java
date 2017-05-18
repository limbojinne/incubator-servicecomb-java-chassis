/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.common.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;

import com.huawei.paas.foundation.common.config.impl.IncConfigs;
import com.huawei.paas.foundation.common.config.impl.PropertiesLoader;
import com.huawei.paas.foundation.common.config.impl.XmlLoader;
import com.huawei.paas.foundation.common.config.impl.XmlLoaderUtils;
import com.huawei.paas.foundation.common.config.impl.IncConfigs.IncConfig;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ConfigMgr {
    /**
     * ConfigMgr instance
     */
    public static final ConfigMgr INSTANCE = new ConfigMgr();

    private Map<String, Object> configMap = new ConcurrentHashMap<>();

    private Map<String, ConfigLoader> configLoaderMap;

    /**
     * 扫描所有的*.inc.config.xml
     * 根据配置初始化所有的loader
     * @throws Exception Exception
     */
    public void init() throws Exception {
        List<Resource> resArr =
            PaaSResourceUtils.getSortedResources("classpath*:config/config.inc.xml", ".inc.xml");
        IncConfigs incConfigs = new IncConfigs();
        incConfigs.setPropertiesList(new ArrayList<>());
        incConfigs.setXmlList(new ArrayList<>());
        for (Resource resource : resArr) {
            IncConfigs tmp = XmlLoaderUtils.load(resource, IncConfigs.class);
            if (tmp.getPropertiesList() != null) {
                incConfigs.getPropertiesList().addAll(tmp.getPropertiesList());
            }
            if (tmp.getXmlList() != null) {
                incConfigs.getXmlList().addAll(tmp.getXmlList());
            }
        }

        configLoaderMap = new HashMap<>();
        for (IncConfig incConfig : incConfigs.getPropertiesList()) {
            PropertiesLoader loader = (PropertiesLoader) configLoaderMap.get(incConfig.getId());
            if (loader != null) {
                loader.getLocationPatternList().addAll(incConfig.getPathList());
                continue;
            }

            configLoaderMap.put(incConfig.getId(), new PropertiesLoader(incConfig.getPathList()));
        }

        for (IncConfig incConfig : incConfigs.getXmlList()) {
            XmlLoader loader = (XmlLoader) configLoaderMap.get(incConfig.getId());
            if (loader != null) {
                loader.getLocationPatternList().addAll(incConfig.getPathList());
                continue;
            }
            configLoaderMap.put(incConfig.getId(), new XmlLoader(incConfig.getPathList()));
        }
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param configId configId
     * @param <T> T
     * @return T
     * @throws Exception Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String configId) throws Exception {
        T config = (T) configMap.get(configId);
        if (config != null) {
            return config;
        }

        if (configLoaderMap == null) {
            init();
        }

        ConfigLoader loader = configLoaderMap.get(configId);
        if (loader == null) {
            throw new RuntimeException("can not find config for " + configId);
        }

        config = (T) loader.load();
        configMap.put(configId, config);
        return config;
    }
}
