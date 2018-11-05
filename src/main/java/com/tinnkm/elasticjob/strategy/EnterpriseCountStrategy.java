package com.tinnkm.elasticjob.strategy;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.tinnkm.elasticjob.utils.GenericSuperclassUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 11:32
 * @Description: 根据企业总数进行分片
 * @since: 1.0
 */
@Slf4j
public abstract class EnterpriseCountStrategy<T> implements SimpleJob {
    private MongoTemplate mongoTemplate;

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("当前执行的任务是：{}",shardingContext.getJobName());
        log.info("当前分片是：{}",shardingContext.getShardingItem());
        log.info("分片总数是：{}",shardingContext.getShardingTotalCount());
        Long count = getCount();
        List<T> enterpriseList = getEnterpriseList(shardingContext.getShardingItem(), Integer.parseInt(Long.toString(count / shardingContext.getShardingTotalCount())));
        handle(enterpriseList,shardingContext.getJobParameter(),shardingContext.getShardingParameter());
    }
    protected abstract void handle(List<T> enterprises, String JobParameter, String shardingParameter);

    protected Long getCount(){
        return mongoTemplate.count(new Query(Criteria.where("ishz").is("1")), "enterprise");
    }

    protected List<T> getEnterpriseList(Integer page,Integer row){
        Query query = new Query(Criteria.where("ishz").is("1"));
        query.skip(page  * row);
        query.limit(row);
        return mongoTemplate.find(query, GenericSuperclassUtil.<T>getActualTypeArgument(this.getClass()));
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
