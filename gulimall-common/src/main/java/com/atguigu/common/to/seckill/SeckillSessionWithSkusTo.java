package com.atguigu.common.to.seckill;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 秒杀场次信息
 **/

@Data
public class SeckillSessionWithSkusTo {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuRelationTo> relationSkus;
}
