package com.tinnkm.elasticjob.controller;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.tinnkm.elasticjob.config.SimpleJobConfig.PACKAGE_NAME;

@RestController
@RequestMapping("/elastic-job")
public class registerController {
    @Autowired
    private ZookeeperRegistryCenter regCenter;

    @Autowired
    private JobEventConfiguration jobEventConfiguration;

    @PostMapping("/register")
    public String register(@RequestBody JobSettings jobSettings){

        // 通过反射获取实现所有实现
        SimpleJob job = null;
        try {
            Class<? extends SimpleJob> jobClass = (Class<? extends SimpleJob>) Class.forName(jobSettings.getJobClass());
            job = jobClass.newInstance();
//                Method setMongoTemplate = job.getClass().getMethod("setMongoTemplate", MongoTemplate.class);
//                setMongoTemplate.invoke(job,mongoTemplate);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException  e) {
            e.printStackTrace();
            return "当前工程下没有job实现类，请检查ip是否正确";
        }
        new SpringJobScheduler(job, regCenter,getLiteJobConfiguration(job.getClass(), jobSettings.getJobName(),jobSettings.getCron(), Integer.valueOf(jobSettings.getShardingTotalCount()), jobSettings.getShardingItemParameters(), PACKAGE_NAME +jobSettings.getJobShardingStrategyClass(),jobSettings.getJobParameter()),jobEventConfiguration ).init();
        return "success";
    }

    private LiteJobConfiguration getLiteJobConfiguration(final Class<? extends SimpleJob> jobClass,final String jobName, final String cron, final int shardingTotalCount, final String shardingItemParameters,String strategyClass,String jobParameter) {
        if (strategyClass.contains("PerformanceStrategy")){
            return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                    jobName, cron, shardingTotalCount).jobParameter(jobParameter).shardingItemParameters(shardingItemParameters).failover(true).build(), jobClass.getCanonicalName())).monitorExecution(true).monitorPort(9888).reconcileIntervalMinutes(10).overwrite(true).jobShardingStrategyClass(strategyClass).build();
        }
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                jobName, cron, shardingTotalCount).jobParameter(jobParameter).shardingItemParameters(shardingItemParameters).failover(true).build(), jobClass.getCanonicalName())).monitorPort(9888).monitorExecution(true).reconcileIntervalMinutes(10).overwrite(true).build();
    }
}
