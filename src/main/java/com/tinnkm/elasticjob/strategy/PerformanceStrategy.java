package com.tinnkm.elasticjob.strategy;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.env.IpUtils;
import com.tinnkm.elasticjob.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/31 12:07
 * @Description: 根据性能进行分片(JobShardingStrategy这个类只有leader会执行)
 * @since: 1.0
 */
@Slf4j
public class PerformanceStrategy implements JobShardingStrategy {

    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount) {
        Map<JobInstance, List<Integer>> result = new HashMap<>();
        // 2.从zk中获取各ip的剩余内存值
        HashMap<String, Long> map = new HashMap<>();
        jobInstances.forEach(jobInstance -> {
            try {
                String ip = jobInstance.getIp();
                Long freeMemory = getSystemInfoFromZk(ip);
                log.info("ip is:{},freeMemory is {}",ip,freeMemory);
                map.put(ip,freeMemory);
            } catch (Exception e) {
                log.error("get system info from zk failed:{}",e.getMessage());
            }
        });
        long totalFreeMemory = map.entrySet().stream().mapToLong(Map.Entry::getValue).sum();
        // 根据内存进行分片，内存剩余大的，获得的分片越多，内存越小获得的分片越小
        long count = 0;
        for (int i = 0; i < jobInstances.size() ; i++) {
            JobInstance jobInstance = jobInstances.get(i);
            Long freeMemory = map.get(jobInstance.getIp());
            if (count < shardingTotalCount){
                // 求出一个近似片
                long round = Math.round(freeMemory * 1.0 / totalFreeMemory * shardingTotalCount);
                if (round > shardingTotalCount - count ){
                    result.put(jobInstance,getList(count,shardingTotalCount - count));
                    break;
                }
                result.put(jobInstance,getList(count,round));
                count += round;
            }
        }
        return result;
    }

    private List<Integer> getList(Long begin,long shading){
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < shading; i++) {
            list.add(i+begin.intValue());
        }
        return list;
    }



    /**
     * 从zk中读取系统信息
     * @param ip
     */
    private Long getSystemInfoFromZk(String ip){
        ZookeeperRegistryCenter zookeeperRegistryCenter = getZookeeperRegistryCenter();
        if (zookeeperRegistryCenter.isExisted("/"+ip)){
            return Long.valueOf(zookeeperRegistryCenter.get("/"+ip));
        }
        zookeeperRegistryCenter.close();
       return 0L;
    }


    private ZookeeperRegistryCenter getZookeeperRegistryCenter(){
        ZookeeperRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(PropertiesUtil.getConfig("elastic-job.serverList"), PropertiesUtil.getConfig("elastic-job.namespace")));
        zookeeperRegistryCenter.init();
        return zookeeperRegistryCenter;
    }
}
