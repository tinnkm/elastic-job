package com.tinnkm.elasticjob.entry;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 14:43
 * @Description: TODO
 * @since: 1.0
 */
@Document(collection="item_region")
public class ItemRegion {
    @Field("ent_id")
    private Long entId;

    @Field("region_code")
    private String regionCode;

    public Long getEntId() {
        return entId;
    }

    public void setEntId(Long entId) {
        this.entId = entId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
}
