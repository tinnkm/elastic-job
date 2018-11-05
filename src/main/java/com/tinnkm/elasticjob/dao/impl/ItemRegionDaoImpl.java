package com.tinnkm.elasticjob.dao.impl;

import com.tinnkm.elasticjob.dao.ItemRegionDao;
import com.tinnkm.elasticjob.entry.ItemRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 14:47
 * @Description: itemRegion 实现类
 * @since: 1.0
 */
@Repository
public class ItemRegionDaoImpl implements ItemRegionDao {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public Long getCountByEntId(Long entId) {
        Query query = new Query(Criteria.where("ent_id").is(entId));
        return mongoTemplate.count(query,ItemRegion.class);
    }
}
