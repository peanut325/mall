package com.atguigu.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 锁定库存成功，往延时队列存入 工作单to 对象
 * wms_ware_order_task
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockLockedTo {

    /** 库存工作单的id **/
    private Long id;

    /** 库存单详情 wms_ware_order_task_detail**/
    private StockDetailTo detail;
}
