package com.tinnkm.elasticjob.dao.impl;

import com.tinnkm.elasticjob.ElasticJobApplicationTests;
import com.tinnkm.elasticjob.dao.EnterpriseDao;
import com.tinnkm.elasticjob.entry.Enterprise;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class EnterpriseDaoImplTest extends ElasticJobApplicationTests {

    @Autowired
    private EnterpriseDao enterpriseDao;
    @Test
    public void getCount() {
        Long count = enterpriseDao.getCount();
        assertNotNull(count);
    }
    @Test
    public void getEnterpriseList() {
        List<Enterprise> enterpriseList = enterpriseDao.getEnterpriseList(1, 10);
        assertNotNull(enterpriseList);
    }

}