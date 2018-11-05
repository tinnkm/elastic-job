package com.tinnkm.elasticjob.utils;

import com.tinnkm.elasticjob.ElasticJobApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class PropertiesUtilTest  extends ElasticJobApplicationTests {

    @Test
    public void testConfig(){
        String config = PropertiesUtil.getConfig("elastic-job.serverList");
        log.info(config);
    }
}