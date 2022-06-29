package com.atguigu.common.to.ware;

import lombok.Data;

/**
 * TODO 废弃
 * 库存锁定结果，每一个Item一个结果
 */
@Data
public class LockStockResultTo {
    private Long skuId;
    private Integer num;
    /**
     * 是否锁定成功
     **/
    private Boolean locked;
}
