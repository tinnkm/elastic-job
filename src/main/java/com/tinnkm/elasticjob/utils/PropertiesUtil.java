package com.tinnkm.elasticjob.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Properties;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/31 16:41
 * @Description: 获取properties值
 * @since: 1.0
 */
@Slf4j
public class PropertiesUtil {
    public static String getConfig(String filePath,String key) {
        try(InputStream is = PropertiesUtil.class.getResourceAsStream(filePath)) {
            Properties properties = new Properties();
            properties.load(is);
            return properties.getProperty(key);
        } catch (IOException e) {
            log.error("get property failed:{}",e.getMessage());
        }
        return null;
    }

    public static String getConfig(String key) {
        return getConfig("/application.properties",key);
    }
}
