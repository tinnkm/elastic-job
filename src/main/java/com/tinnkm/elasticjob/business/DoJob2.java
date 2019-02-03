package com.tinnkm.elasticjob.business;

import com.tinnkm.elasticjob.entry.Enterprise;
import com.tinnkm.elasticjob.strategy.EnterpriseCountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/11/2 14:12
 * @Description: TODO
 * @since: 1.0
 */

public class DoJob2 extends EnterpriseCountStrategy<Enterprise> {

    @Override
    protected void handle(List<Enterprise> enterprises, String jobParameter, String shardingParameter) {
        System.out.println("----------------job2 done---------------");
        System.out.println("------------------job2的参数----------------------");
        System.out.println(jobParameter);
        System.out.println("------------------job2的参数----------------------");
        // 具体的业务实现
        enterprises.forEach(enterprise -> System.out.println(enterprise.getEntId()));
    }
}
