package com.atguigu.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 结算页VO（confirm.html需要的页面数据）
 *
 * @author: wan
 */
public class OrderConfirmVo {

    /**
     * 会员收获地址列表，ums_member_receive_address
     **/
    @Getter
    @Setter
    List<MemberAddressVo> memberAddressVos;

    /**
     * 所有选中的购物项【购物车中的选中项】
     **/
    @Getter
    @Setter
    List<OrderItemVo> items;

    /**
     * 优惠券（会员积分）
     **/
    @Getter
    @Setter
    private Integer integration;

    /**
     * TODO 防止重复提交的令牌 幂等性
     **/
    @Getter
    @Setter
    private String uniqueToken;

    /**
     * 库存
     * 有货/无货，不放在item里面
     */
    @Getter
    @Setter
    Map<Long, Boolean> stocks;

    /**
     * 总商品金额
     **/
    //BigDecimal total;
    //计算订单总额
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                // 计算当前商品总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount()));
                // 累加全部商品总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }

    /**
     * 应付总额
     **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }

    /**
     * 商品总数
     */
    public Integer getCount() {
        Integer count = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

}
