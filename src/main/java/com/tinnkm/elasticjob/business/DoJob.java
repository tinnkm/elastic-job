package com.tinnkm.elasticjob.business;

import com.tinnkm.elasticjob.entry.Enterprise;
import com.tinnkm.elasticjob.strategy.ItemRegionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 16:34
 * @Description: 面向用户的具体实现
 * @since: 1.0
 */
public class DoJob extends ItemRegionStrategy<Enterprise,Long> {


    @Override
    protected void handle(List<Enterprise> enterprises, String JobParameter, String shardingParameter) {
        System.out.println("----------------job done---------------");
        // 具体的业务实现
        enterprises.forEach(enterprise -> System.out.println(enterprise.getEntId()));
    }

    @Override
    protected Long getBusinessEntryKey(Enterprise enterprise) {
        return enterprise.getEntId();
    }

}
