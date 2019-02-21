package com.tinnkm.elasticjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.tinnkm.elasticjob.config.SimpleJobConfig.PACKAGE_NAME;

/**
 * @Auther: tinnkm
 * @Date: 2018/11/1 16:29
 * @Description: TODO
 * @since: 1.0
 */
@Component
@Slf4j
public class JobApplicaionRunner implements ApplicationRunner  {
    @Autowired
    private ZookeeperRegistryCenter regCenter;
    @Autowired
    Environment environment;
    @Autowired
    private JobEventConfiguration jobEventConfiguration;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 注册zk
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                saveSystemInfoToZk();
            }
        } ,0,1000*60*1);
        // 默认加载所有
        jobInit();
        // 监听节点变化
        watchNode();
    }

    private void jobInit() {
        List<String> childrenKeys = regCenter.getChildrenKeys("/register-job-node");
        childrenKeys.forEach(childrenKey -> {
            String jobSettingStr = regCenter.get("/register-job-node/"+childrenKey);
            if (!StringUtils.isEmpty(jobSettingStr)){
                addJob(jobSettingStr);
            }

        });
    }

    private void addJob(String jobSettingStr) {
        LiteJobConfiguration liteJobConfiguration = LiteJobConfigurationGsonFactory.fromJson(jobSettingStr);
        ObjectMapper objectMapper = new ObjectMapper();
        JobSettings jobSettings = null;
        try {
            jobSettings = objectMapper.readValue(jobSettingStr, JobSettings.class);
        } catch (IOException e) {
            log.error("节点存储数据有误！",e);
        }
        // 通过反射获取实现所有实现
        SimpleJob job = null;
        try {
            Class<? extends SimpleJob> jobClass = (Class<? extends SimpleJob>) Class.forName(jobSettings.getJobClass());
            job = jobClass.newInstance();
            Method setMongoTemplate = job.getClass().getMethod("setMongoTemplate", MongoTemplate.class);
            setMongoTemplate.invoke(job,mongoTemplate);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            log.error("当前工程下没有job实现类，请检查ip是否正确",e);
        }
        new SpringJobScheduler(job, regCenter,liteJobConfiguration,jobEventConfiguration ).init();
    }

    private void watchNode() throws Exception {
        CuratorFramework client = regCenter.getClient();
        TreeCache treeCache = TreeCache.newBuilder(client, "/register-job-node").setCacheData(true).setMaxDepth(1).build();
        treeCache.getListenable().addListener((curatorFramework,treeCacheEvent) ->{
            ChildData data = treeCacheEvent.getData();
            TreeCacheEvent.Type eventType = treeCacheEvent.getType();
            if (data != null){
                String jobSettingStr = new String(data.getData());
                JobSettings jobSettings = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    jobSettings = objectMapper.readValue(jobSettingStr, JobSettings.class);
                } catch (IOException e) {
                    log.error("数据转换异常！",e);
                }
                if (null == jobSettings){
                    throw new IllegalArgumentException("节点存储数据有误！");
                }
                switch (eventType){
                    case NODE_ADDED:
                        // 添加job
                        addJob(jobSettingStr);
                        break;
                    case NODE_UPDATED:
                        // 更新job
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getJobName()), "jobName can not be empty.");
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobSettings.getCron()), "cron can not be empty.");
                        Preconditions.checkArgument(jobSettings.getShardingTotalCount() > 0, "shardingTotalCount should larger than zero.");
                        JobNodePath jobNodePath = new JobNodePath(jobSettings.getJobName());
                        regCenter.update(jobNodePath.getConfigNodePath(), LiteJobConfigurationGsonFactory.toJsonForObject(jobSettings));
                        break;
                    case NODE_REMOVED:
                        // 删除job
                        if (!StringUtils.isEmpty(jobSettings.getJobName())){
                            JobNodePath jobNodePaths = new JobNodePath(jobSettings.getJobName());
                            for (String each : regCenter.getChildrenKeys(jobNodePaths.getInstancesNodePath())) {
                                regCenter.remove(jobNodePaths.getInstanceNodePath(each));
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        treeCache.start();
    }

    /**
     * 将系统信息写入zk
     */
    private void saveSystemInfoToZk(){
        // 本机ip
        String ip = IpUtils.getIp();
        // 剩余内存
        long freeMemory = Runtime.getRuntime().freeMemory();
        log.info("{} freeMemory is {}",ip,freeMemory);
        regCenter.persistEphemeral("/"+ip,String.valueOf(freeMemory));
    }


}
