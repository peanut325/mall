package com.atguigu.gulimall.gulimallcart.vo;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车VO
 * 需要计算的属性需要重写get方法，保证每次获取属性都会进行计算
 */
public class CartVo {

    private List<CartItemVo> items; // 购物项集合
    private Integer countNum;       // 商品件数（汇总购物车内商品总件数）
    private Integer countType;      // 商品数量（汇总购物车内商品总个数）
    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal("0.00");// 减免价格

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        return CollectionUtils.isEmpty(items) ? 0 : items.size();
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 1、计算购物项总价
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItemVo cartItem : items) {
                if (cartItem.getCheck()) {
                    amount = amount.add(cartItem.getTotalPrice());
                }
            }
        }
        // 2、计算优惠后的价格
        return amount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
