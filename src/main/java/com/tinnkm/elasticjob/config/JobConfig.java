package com.tinnkm.elasticjob.config;

/**
 * @Auther: tinnkm
 * @Date: 2018/11/2 12:02
 * @Description: TODO
 * @since: 1.0
 */
public class JobConfig {
    /**
     * 策略
     */
    private String strategyClass;
    /**
     * 作业名
     */
    private String jobName;
    /**
     * cron表达式
     */
    private String cron;

    /**
     * 分片总数
     */
    private Integer shardingTotalCount;

    /**
     * 分片参数
     */
    private String shardingItemParameters;

    /**
     * job参数
     */
    private String jobParameters;

    /**
     * job的全量类名
     */
    private String jobClass;

    public String getStrategyClass() {
        return strategyClass;
    }

    public void setStrategyClass(String strategyClass) {
        this.strategyClass = strategyClass;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Integer getShardingTotalCount() {
        return shardingTotalCount;
    }

    public void setShardingTotalCount(Integer shardingTotalCount) {
        this.shardingTotalCount = shardingTotalCount;
    }

    public String getShardingItemParameters() {
        return shardingItemParameters;
    }

    public void setShardingItemParameters(String shardingItemParameters) {
        this.shardingItemParameters = shardingItemParameters;
    }

    public String getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }
}
