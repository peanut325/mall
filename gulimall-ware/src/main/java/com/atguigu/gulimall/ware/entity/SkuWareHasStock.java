package com.atguigu.gulimall.ware.entity;

import lombok.Data;

import java.util.List;

@Data
public class SkuWareHasStock {

    private Long skuId; // 此货物skuId
    private List<Long> wareIds;  // 在哪个仓库有货
    private int lockNum;    // 需要锁定数量

}
