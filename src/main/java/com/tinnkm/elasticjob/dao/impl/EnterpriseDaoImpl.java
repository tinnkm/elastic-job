package com.tinnkm.elasticjob.dao.impl;

import com.tinnkm.elasticjob.dao.EnterpriseDao;
import com.tinnkm.elasticjob.entry.Enterprise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 12:01
 * @Description: 企业dao实现类
 * @since: 1.0
 */
@Repository
public class EnterpriseDaoImpl implements EnterpriseDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long getCount() {
        return mongoTemplate.count(new Query(Criteria.where("ishz").is("1")), "enterprise");
    }

    @Override
    public List<Enterprise> getAll() {
        Query query = new Query(Criteria.where("ishz").is("1"));
        return mongoTemplate.find(query, Enterprise.class);
    }

    @Override
    public List<Enterprise> getEnterpriseList(int page, int row) {
        Query query = new Query(Criteria.where("ishz").is("1"));
        query.skip(page  * row);
        query.limit(row);
        return mongoTemplate.find(query, Enterprise.class);
    }
}
