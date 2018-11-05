package com.tinnkm.elasticjob.dao.impl;

import com.tinnkm.elasticjob.ElasticJobApplicationTests;
import com.tinnkm.elasticjob.dao.EnterpriseDao;
import com.tinnkm.elasticjob.dao.ItemRegionDao;
import com.tinnkm.elasticjob.entry.Enterprise;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
public class ItemRegionDaoImplTest extends ElasticJobApplicationTests {

    @Autowired
    private EnterpriseDao enterpriseDao;

    @Autowired
    private ItemRegionDao itemRegionDao;
    @Test
    public void getCountByEntId() {
        List<Enterprise> all = enterpriseDao.getAll();
        Long count = itemRegionDao.getCountByEntId(all.get(0).getEntId());
        assertNotNull(count);
    }
}