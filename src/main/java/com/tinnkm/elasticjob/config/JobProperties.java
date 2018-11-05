package com.tinnkm.elasticjob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 12:20
 * @Description: 任务配置类
 * @since: 1.0
 */
@ConfigurationProperties(prefix = "elastic-job")
public class JobProperties {

    /**
     * job集合
     */
    private List<JobConfig> jobList;
    /**
     * zk列表
     */
    private String serverList;


    /**
     * zk命名空间
     */
    private String namespace;





    public String getServerList() {
        return serverList;
    }

    public void setServerList(String serverList) {
        this.serverList = serverList;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<JobConfig> getJobList() {
        return jobList;
    }

    public void setJobList(List<JobConfig> jobList) {
        this.jobList = jobList;
    }
}
