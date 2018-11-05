package com.tinnkm.elasticjob.entry;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/30 14:22
 * @Description: 企业实体类
 * @since: 1.0
 */
@Document(collection = "enterprise")
public class Enterprise {
    @Field("ent_id")
    private Long entId;

    public Long getEntId() {
        return entId;
    }

    public void setEntId(Long entId) {
        this.entId = entId;
    }
}
