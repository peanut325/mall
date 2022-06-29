package com.atguigu.gulimall.gulimallcart.service;

import com.atguigu.gulimall.gulimallcart.vo.CartItemVo;
import com.atguigu.gulimall.gulimallcart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    /**
     * 添加购物项
     *
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItemVo addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物项
     *
     * @param skuId
     * @return
     */
    CartItemVo getCartBySkuId(Long skuId);

    /**
     * 获取购物车
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartVo getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     *
     * @param cartKey
     */
    public void clearCart(String cartKey);

    /**
     * 勾选购物项
     *
     * @param skuId
     * @param checked
     */
    void checkItem(Long skuId, Integer checked);

    /**
     * 修改购物项数量
     *
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物项
     *
     * @param skuId
     */
    void deleteItemBySkuId(Long skuId);

    /**
     * 查询用户购物车的购物项
     *
     * @return
     */
    List<CartItemVo> getCartItems();
}
