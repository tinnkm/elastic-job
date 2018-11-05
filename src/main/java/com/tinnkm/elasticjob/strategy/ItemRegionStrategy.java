package com.tinnkm.elasticjob.strategy;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.tinnkm.elasticjob.utils.GenericSuperclassUtil;
import com.tinnkm.elasticjob.utils.ListUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 16:57
 * @Description: 根据itemRegion策略进行数据分片
 * @since: 1.0
 */
@Slf4j
public abstract class ItemRegionStrategy<T,R> implements SimpleJob {
    private MongoTemplate mongoTemplate;


    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("当前执行的任务是：{}",shardingContext.getJobName());
        log.info("当前分片是：{}", shardingContext.getShardingItem());
        log.info("分片总数是：{}", shardingContext.getShardingTotalCount());
        // 获取所有企业
        List<T> all = getAll();
        List<R> entIds = all.stream().map(this::getBusinessEntryKey).collect(Collectors.toList());
        // 获取对应商品的总数
        HashMap<R, Long> countMap = new HashMap<>();
        entIds.forEach(id -> {
            Long count = getCountByEntId(id);
            countMap.put(id, count);
        });
        // 根据总数分组
        Map<Long, List<Long>> integerListHashMap = ListUtils.getDivideList((Map<Long, Long>) countMap, shardingContext.getShardingTotalCount());
        List<Long> integers = integerListHashMap.get((long) shardingContext.getShardingItem());
        List<T> enterpriseList = all.stream().filter(enterprise -> {
            R businessEntryKey = getBusinessEntryKey(enterprise);
            return integers.contains(businessEntryKey);
        }).collect(Collectors.toList());
        handle(enterpriseList, shardingContext.getJobParameter(), shardingContext.getShardingParameter());
    }

    protected abstract void handle(List<T> enterprises, String JobParameter, String shardingParameter);
    protected abstract R getBusinessEntryKey(T t);

    protected Long getCountByEntId(R id){
        Query query = new Query(Criteria.where("ent_id").is(id));
        return mongoTemplate.count(query, "item_region");
    }
    protected List<T> getAll(){
        Query query = new Query(Criteria.where("ishz").is("1"));
        return mongoTemplate.find(query, GenericSuperclassUtil.<T>getActualTypeArgument(this.getClass()));
    }
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
