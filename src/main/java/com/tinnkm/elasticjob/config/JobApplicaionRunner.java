package com.tinnkm.elasticjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        // 监听节点变化
        watchNode();
    }

    private void watchNode() throws Exception {
        CuratorFramework client = regCenter.getClient();
        TreeCache treeCache = TreeCache.newBuilder(client, "/register-job-node").setCacheData(true).setMaxDepth(1).build();
        treeCache.getListenable().addListener((curatorFramework,treeCacheEvent) ->{
            ChildData data = treeCacheEvent.getData();
            TreeCacheEvent.Type eventType = treeCacheEvent.getType();
            if (data != null){
                if (eventType.equals(TreeCacheEvent.Type.NODE_ADDED) || eventType.equals(TreeCacheEvent.Type.NODE_UPDATED)){
                    List<String> childrenKeys = regCenter.getChildrenKeys("/register-job-node");
                    childrenKeys.forEach(childrenKey -> {
                        String jobSettingStr = regCenter.get("/register-job-node/"+childrenKey);
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
                    });
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
