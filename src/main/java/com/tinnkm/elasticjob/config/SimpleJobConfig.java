/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.tinnkm.elasticjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Configuration
@EnableConfigurationProperties(JobProperties.class)
public class SimpleJobConfig {

    public static final String PACKAGE_NAME = "com.tinnkm.elasticjob.strategy.";
    @Autowired
    private ZookeeperRegistryCenter regCenter;
    
    @Autowired
    private JobEventConfiguration jobEventConfiguration;

    @Autowired
    private JobProperties jobProperties;

    @Autowired
    private MongoTemplate mongoTemplate;
    @PostConstruct
    public void init() {
        List<JobConfig> jobList = jobProperties.getJobList();
        if (null == jobList || jobList.size() == 0){
            throw new IllegalArgumentException("jobList can't be null");
        }
        jobList.forEach(jobConfig -> {
            // 通过反射获取实现所有实现
            SimpleJob job = null;
            try {
                Class<? extends SimpleJob> jobClass = (Class<? extends SimpleJob>) Class.forName(jobConfig.getJobClass());
                job = jobClass.newInstance();
                Method setMongoTemplate = job.getClass().getMethod("setMongoTemplate", MongoTemplate.class);
                setMongoTemplate.invoke(job,mongoTemplate);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            new SpringJobScheduler(job, regCenter, getLiteJobConfiguration(job.getClass(), jobConfig.getJobName(),jobConfig.getCron(), jobConfig.getShardingTotalCount(), jobConfig.getShardingItemParameters(), PACKAGE_NAME +jobConfig.getStrategyClass()), jobEventConfiguration).init();
        });

    }
    
    private LiteJobConfiguration getLiteJobConfiguration(final Class<? extends SimpleJob> jobClass,final String jobName, final String cron, final int shardingTotalCount, final String shardingItemParameters,String strategyClass) {
        if (strategyClass.contains("PerformanceStrategy")){
            return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                    jobName, cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).build(), jobClass.getCanonicalName())).overwrite(true).jobShardingStrategyClass(strategyClass).build();
        }
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                jobName, cron, shardingTotalCount).shardingItemParameters(shardingItemParameters).failover(true).build(), jobClass.getCanonicalName())).overwrite(true).build();
    }
}
