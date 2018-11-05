package com.tinnkm.elasticjob.dao;

import com.tinnkm.elasticjob.entry.ItemRegion;

import java.util.List;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 14:45
 * @Description: 商品dao
 * @since: 1.0
 */
public interface ItemRegionDao {
    /**
     * 获取ItemRegion的总数
     * @return
     */
    Long getCountByEntId(Long entId);

}
