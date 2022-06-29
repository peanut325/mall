package com.atguigu.gulimall.gulimallcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.cart.CartConstant;
import com.atguigu.common.to.cart.SkuInfoTo;
import com.atguigu.common.to.cart.UserInfoTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallcart.client.ProductFeignService;
import com.atguigu.gulimall.gulimallcart.interceptor.CartInterceptor;
import com.atguigu.gulimall.gulimallcart.service.CartService;
import com.atguigu.gulimall.gulimallcart.vo.CartItemVo;
import com.atguigu.gulimall.gulimallcart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CartItemVo addCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {
            // 如果redis中没有购物车，添加购物车
            CartItemVo cartItem = new CartItemVo();
            // 使用异步编排
            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                // 远程调用查询sku基本信息
                R r = productFeignService.info(skuId);
                SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {
                });
                cartItem.setSkuId(skuInfo.getSkuId());// 商品ID
                cartItem.setTitle(skuInfo.getSkuTitle());// 商品标题
                cartItem.setImage(skuInfo.getSkuDefaultImg());// 商品默认图片
                cartItem.setPrice(skuInfo.getPrice());// 商品单价
                cartItem.setCount(num);// 商品件数
                cartItem.setCheck(true);// 是否选中
            }, threadPoolExecutor);

            CompletableFuture<Void> getSkuAttrValuesFuture = CompletableFuture.runAsync(() -> {
                // 远程调用查询sku销售属性
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(skuSaleAttrValues);
            }, threadPoolExecutor);

            // 等待两个线程都完成才保存到redis
            CompletableFuture.allOf(getSkuInfoFuture, getSkuAttrValuesFuture).get();

            // 以json格式保存在redis中
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);

            return cartItem;
        } else {
            // 如果redis中有数据，那么就更新数量
            CartItemVo cartItem = JSON.parseObject(res, CartItemVo.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItemVo getCartBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOps = getCartOps();
        String data = (String) hashOps.get(skuId.toString());
        return JSON.parseObject(data, CartItemVo.class);
    }

    /**
     * 查询购物车
     *
     * @return
     */
    @Override
    public CartVo getCart() throws ExecutionException, InterruptedException {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 先判断零时购物车是否有商品
            String tempCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> tempCartItemList = getCartItems(tempCartKey);
            if (tempCartItemList != null && tempCartItemList.size() > 0) {
                for (CartItemVo cartItemVo : tempCartItemList) {
                    // 合并在用户购物车中
                    addCart(cartItemVo.getSkuId(), cartItemVo.getCount());
                }
                // 删除临时购物车
                clearCart(tempCartKey);
            }
            // 使用用户的id作为购物车，此时购物车已经合并了，所以直接查
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            cartVo.setItems(getCartItems(cartKey));
        } else {
            // 此时没登录，就用cookie，游客购物车
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            // 此时游客购物车直接查询遍历即可
            List<CartItemVo> cartItemVoList = getCartItems(cartKey);
            cartVo.setItems(cartItemVoList);
        }

        return cartVo;
    }

    /**
     * 根据cartKey获取购物车所有商品
     *
     * @param cartKey
     * @return
     */
    private List<CartItemVo> getCartItems(String cartKey) {
        // 绑定购物车的key操作Redis
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> cartItemVoList = values.stream().map((obj) -> {
                CartItemVo cartItemVo = JSON.parseObject(String.valueOf(obj), CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return cartItemVoList;
        }
        return null;
    }

    /**
     * 根据cartKey删除购物车
     *
     * @param cartKey
     */
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartBySkuId = getCartBySkuId(skuId);
        cartBySkuId.setCheck(checked == 1 ? true : false);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartBySkuId));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartBySkuId = getCartBySkuId(skuId);
        cartBySkuId.setCount(num);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartBySkuId));
    }

    @Override
    public void deleteItemBySkuId(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            // 从redis中查出
            List<CartItemVo> cartItems = getCartItems(CartConstant.CART_PREFIX + userInfoTo.getUserId());
            // 过滤出选中的，且更新最新的价格
            List<CartItemVo> collect = cartItems.stream().filter(CartItemVo::getCheck).map((item) -> {
                // 远程调用查询
                R r = productFeignService.getSkuPrice(item.getSkuId());
                String price = (String) r.get("data");
                item.setPrice(new BigDecimal(price));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }

    /**
     * 根据key兵丁hash操作
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 此时登录了，使用用户的id作为购物车
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            // 此时没登录，就用cookie，游客购物车
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }

        // 绑定购物车的key操作Redis
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        return hashOps;
    }

}
