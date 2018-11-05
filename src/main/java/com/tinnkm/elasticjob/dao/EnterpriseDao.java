package com.tinnkm.elasticjob.dao;

import com.tinnkm.elasticjob.entry.Enterprise;
import com.tinnkm.elasticjob.entry.ItemRegion;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/29 17:52
 * @Description: 企业实体类dao接口
 * @since: 1.0
 */
public interface EnterpriseDao{
    /**
     * 获取企业总数
     * @return 企业总条数
     */
    Long getCount();

    /**
     * 获取所有的企业
     * @return
     */
    List<Enterprise> getAll();

    /**
     * 获取企业的分页数据
     * @param page 当前页
     * @param row 行数
     * @return 企业总数结果
     */
    List<Enterprise> getEnterpriseList(int page,int row);

}
