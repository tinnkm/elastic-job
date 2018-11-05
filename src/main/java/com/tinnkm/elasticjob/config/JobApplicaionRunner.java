package com.tinnkm.elasticjob.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

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
        // 装配job
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
