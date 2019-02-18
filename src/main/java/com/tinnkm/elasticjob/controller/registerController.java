package com.tinnkm.elasticjob.controller;

import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.tinnkm.elasticjob.utils.ZKUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZKUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/elastic-job")
public class registerController {

    @Value("${elastic-job.serverList}")
    private String serverList;
    @Value("${elastic-job.namespace}")
    private String namespace;

    @PostMapping("/register")
    public String register(@RequestBody JobSettings jobSettings) throws IOException {
        ZKUtils zkUtils = new ZKUtils(serverList, namespace);
        zkUtils.addCacheData("/register-job-node");
        zkUtils.persist("/register-job-node/"+jobSettings.getJobName(),new ObjectMapper().writeValueAsString(jobSettings));
        zkUtils.close();
        return "success";
    }


}
