package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.order.OrderConstant;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.member.MemberRespVo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.to.order.OrderTo;
import com.atguigu.common.to.order.SpuInfoTo;
import com.atguigu.common.to.ware.SkuHasStockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.client.CartServiceClient;
import com.atguigu.gulimall.order.client.MemberServiceClient;
import com.atguigu.gulimall.order.client.ProductServiceClient;
import com.atguigu.gulimall.order.client.WmsServiceClient;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.rabbitmq.client.Channel;
import com.sun.javafx.binding.ObjectConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberServiceClient memberServiceClient;

    @Autowired
    private CartServiceClient cartServiceClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private WmsServiceClient wmsServiceClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    public static ThreadLocal<OrderSubmitVo> orderSubmitThreadLocal = new ThreadLocal();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getOrderConfirm() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVO = new OrderConfirmVo();
        MemberRespVo member = LoginUserInterceptor.threadLocal.get();

        // 先从父线程获取原请求
        RequestAttributes request = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            // 在子线程调用时先共享请求为父线程的请求
            RequestContextHolder.setRequestAttributes(request);
            // 远程调用查用户的地址
            List<MemberAddressVo> memberAddress = memberServiceClient.getMemberAddress(member.getId());
            orderConfirmVO.setMemberAddressVos(memberAddress);
        }, threadPoolExecutor);

        CompletableFuture<Void> getItem = CompletableFuture.runAsync(() -> {
            // 在子线程调用时先共享请求为父线程的请求
            RequestContextHolder.setRequestAttributes(request);
            // 远程调用查用户选中了的购物项
            List<OrderItemVo> cartItems = cartServiceClient.getCartItems();
            orderConfirmVO.setItems(cartItems);
        }, threadPoolExecutor).thenRunAsync(() -> {
            // 远程调用查询库存信息
            // 先收集skuId
            List<OrderItemVo> items = orderConfirmVO.getItems();
            List<Long> skuIdList = items.stream().map(orderItemVo -> orderItemVo.getSkuId()).collect(Collectors.toList());
            R r = wmsServiceClient.getSkuHasStock(skuIdList);
            List<SkuHasStockTo> data = r.getData(new TypeReference<List<SkuHasStockTo>>() {
            });

            // 封装库存信息
            if (data != null) {
                Map<Long, Boolean> collect = data.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
                orderConfirmVO.setStocks(collect);
            }
        });

        // 优惠信息在用户信息中已经保存
        orderConfirmVO.setIntegration(member.getIntegration());

        // 其他数据自动计算

        // TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        // 放入redis中
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVO.setUniqueToken(token);

        // 等待2个异步任务
        CompletableFuture.allOf(getAddress, getItem).get();

        return orderConfirmVO;
    }

    @Override
    public SubmitOrderResponseVo getSubmitOrderResponseVo(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();

        // 获取用户信息
        MemberRespVo member = LoginUserInterceptor.threadLocal.get();
        String submitToken = orderSubmitVo.getUniqueToken();
        orderSubmitThreadLocal.set(orderSubmitVo);

        // Lua脚本含义：
        //  查找KEYS[1]的值，如果等于ARGV[1]，就进行删除，删除成功返回1，删除失败返回0，如果没找到返回0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 使用Lua脚本保证令牌的验证和删除是原子性的
        Long flag = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId()), submitToken);

        if (flag == 1L) {
            // 创建订单
            OrderCreateTo order = createOrder();
            // 验价
            BigDecimal totalAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(totalAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 金额对比成功
                // 保存订单
                saveOrder(order);
                // 锁定库存
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemEntity> orderItems = order.getOrderItems();
                // 封装一个类
                List<OrderItemVo> orderItemVoList = orderItems.stream().map(entity -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(entity.getSkuId());
                    orderItemVo.setTitle(entity.getSkuName());
                    orderItemVo.setCount(entity.getSkuQuantity());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVoList);
                // 远程调用
                R r = wmsServiceClient.lockOrderStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 锁住库存成功
                    submitOrderResponseVo.setOrder(order.getOrder());
                    submitOrderResponseVo.setCode(0);

                    // TODO 远程扣减积分

                    // 发送消息到订单队列中，以后进行关单操作
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    return submitOrderResponseVo;
                } else {
                    submitOrderResponseVo.setCode(3);
                    throw new NoStockException((String) r.get("msg"));
                }
            } else {
                // 金额对比失败
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        } else {
            // 令牌校验失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }

    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    /**
     * 修改订单状态为关单
     *
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 修改订单状态，表示未支付
        OrderEntity entity = getById(orderEntity.getId());
        // 时间过了还未付款，关单
        if (entity.getStatus() == 0) {
            OrderEntity updateEntity = new OrderEntity();
            updateEntity.setId(entity.getId());
            updateEntity.setStatus(4);
            updateById(updateEntity);
        }
        // 主动发起解锁库存消息，防止有延时导致库存未解锁，却又修改了订单
        OrderTo orderTo = new OrderTo();
        BeanUtils.copyProperties(entity, orderTo);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        // 保留小数点后两位，向上取
        BigDecimal totalAmount = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(totalAmount.toString());
        payVo.setOut_trade_no(order.getOrderSn());
        // 订单名字，可以取一个商品名，我随机模拟一个
        List<OrderItemEntity> entityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = entityList.get(0);
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> list = page.getRecords().stream().map(item -> {
            List<OrderItemEntity> orderItemEntityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setItemEntities(orderItemEntityList);
            return item;
        }).collect(Collectors.toList());

        page.setRecords(list);

        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        // 保存交易流水
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());

        paymentInfoService.save(paymentInfoEntity);

        // 修改订单的状态信息
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            // 支付成功状态
            String tradeNo = payAsyncVo.getOut_trade_no();
            baseMapper.updateOrderStatus(tradeNo, OrderConstant.OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo order) {
        // 1.创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(order.getOrderSn());
        orderEntity.setMemberId(order.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = order.getSeckillPrice().multiply(BigDecimal.valueOf(order.getNum()));// 应付总额
        orderEntity.setTotalAmount(totalPrice);// 订单总额
        orderEntity.setPayAmount(totalPrice);// 应付总额
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        // 保存订单
        this.save(orderEntity);

        // 2.创建订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(order.getOrderSn());
        orderItem.setRealAmount(totalPrice);
        orderItem.setSkuQuantity(order.getNum());

        // 保存商品的spu信息
        R r = productServiceClient.getSpuBySkuId(order.getSkuId());
        SpuInfoTo spuInfo = r.getData(new TypeReference<SpuInfoTo>() {
        });
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setSpuName(spuInfo.getSpuName());
        orderItem.setSpuBrand(spuInfo.getBrandName());
        orderItem.setCategoryId(spuInfo.getCatalogId());
        // 保存订单项数据
        orderItemService.save(orderItem);
    }

    /**
     * 保存订单
     *
     * @param orderCreateTo
     */
    @Transactional
    void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        this.save(order);
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();

        // 创建订单
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        // 生成订单项实体对象
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 计算价格
        summaryFillOrder(orderEntity, orderItemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(orderItemEntities);

        return orderCreateTo;
    }

    /**
     * 创建订单方法
     *
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        // 设置订单号
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        // 设置memberId
        MemberRespVo member = LoginUserInterceptor.threadLocal.get();
        orderEntity.setMemberId(member.getId());

        // 查找发送地址
        OrderSubmitVo orderSubmitVo = orderSubmitThreadLocal.get();
        R r = wmsServiceClient.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {
        });

        orderEntity.setFreightAmount(fareVo.getFare());
        // 4.封装收货地址信息
        orderEntity.setReceiverName(fareVo.getAddress().getName());// 收货人名字
        orderEntity.setReceiverPhone(fareVo.getAddress().getPhone());// 收货人电话
        orderEntity.setReceiverProvince(fareVo.getAddress().getProvince());// 省
        orderEntity.setReceiverCity(fareVo.getAddress().getCity());// 市
        orderEntity.setReceiverRegion(fareVo.getAddress().getRegion());// 区
        orderEntity.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());// 详细地址
        orderEntity.setReceiverPostCode(fareVo.getAddress().getPostCode());// 收货人邮编
        // 5.封装订单状态信息
        orderEntity.setStatus(OrderConstant.OrderStatusEnum.CREATE_NEW.getCode());
        // 6.设置自动确认时间
        orderEntity.setAutoConfirmDay(OrderConstant.autoConfirmDay);// 7天
        // 7.设置未删除状态
        orderEntity.setDeleteStatus(OrderConstant.OrderIsDeleteEnum.NOT_DELETE.getIsDelete());
        // 8.设置时间
        Date now = new Date();
        orderEntity.setCreateTime(now);
        orderEntity.setModifyTime(now);

        return orderEntity;
    }

    /**
     * 生成订单项实体对象集合
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> cartItems = cartServiceClient.getCartItems();
        List<OrderItemEntity> orderItemEntityList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cartItems)) {
            orderItemEntityList = cartItems.stream()
                    .filter(OrderItemVo::getCheck)
                    .map(item -> buildOrderItem(orderSn, item))
                    .collect(Collectors.toList());
        }
        return orderItemEntityList;
    }

    /**
     * 封装每一个购物项
     *
     * @param orderSn
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(String orderSn, OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        // 1.封装订单号
        itemEntity.setOrderSn(orderSn);
        // 2.封装SPU信息
        R spuInfo = productServiceClient.getSpuBySkuId(cartItem.getSkuId());// 查询SPU信息
        SpuInfoTo spuInfoTO = spuInfo.getData(new TypeReference<SpuInfoTo>() {
        });
        itemEntity.setSpuId(spuInfoTO.getId());
        itemEntity.setSpuName(spuInfoTO.getSpuName());
        itemEntity.setSpuBrand(spuInfoTO.getSpuName());
        itemEntity.setCategoryId(spuInfoTO.getCatalogId());
        // 3.封装SKU信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());// 商品sku图片
        itemEntity.setSkuPrice(cartItem.getPrice());// 这个是最新价格，购物车模块查询数据库得到
        itemEntity.setSkuQuantity(cartItem.getCount());// 当前商品数量
        String skuAttrsVals = String.join(";", cartItem.getSkuAttrValues());
        itemEntity.setSkuAttrsVals(skuAttrsVals);// 商品销售属性组合["颜色:星河银","版本:8GB+256GB"]
        // 4.优惠信息【不做】

        // 5.积分信息
        int num = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue();// 分值=单价*数量
        itemEntity.setGiftGrowth(num);// 成长值
        itemEntity.setGiftIntegration(num);// 积分

        // 6.价格信息
        itemEntity.setPromotionAmount(BigDecimal.ZERO);// 促销金额
        itemEntity.setCouponAmount(BigDecimal.ZERO);// 优惠券金额
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);// 积分优惠金额
        BigDecimal realAmount = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()))
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);// 实际金额，减去所有优惠金额

        return itemEntity;
    }

    /**
     * 汇总封装订单
     * 1.计算订单总金额
     * 2.汇总积分、成长值
     * 3.汇总应付总额 = 订单总金额 + 运费
     *
     * @param orderEntity       订单
     * @param orderItemEntities 订单项
     */
    private void summaryFillOrder(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        // 1.订单总额、促销总金额、优惠券总金额、积分优惠总金额
        BigDecimal total = new BigDecimal(0);
        BigDecimal coupon = new BigDecimal(0);
        BigDecimal promotion = new BigDecimal(0);
        BigDecimal integration = new BigDecimal(0);
        // 2.积分、成长值
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;
        for (OrderItemEntity itemEntity : orderItemEntities) {
            total = total.add(itemEntity.getRealAmount());// 订单总额
            coupon = coupon.add(itemEntity.getCouponAmount());// 促销总金额
            promotion = promotion.add(itemEntity.getPromotionAmount());// 优惠券总金额
            integration = integration.add(itemEntity.getIntegrationAmount());// 积分优惠总金额
            giftIntegration = giftIntegration + itemEntity.getGiftIntegration();// 积分
            giftGrowth = giftGrowth + itemEntity.getGiftGrowth();// 成长值
        }
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setIntegration(giftIntegration);// 积分
        orderEntity.setGrowth(giftGrowth);// 成长值

        // 3.应付总额
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));// 订单总额 +　运费
    }

    /**
     * queues:声明需要监听的所有队列
     * <p>
     * org.springframework.amqp.core.Message
     * <p>
     * 参数可以写一下类型
     * 1、Message message:原生消息详细信息。头+体
     * 2、T<发送的消息的类型>orderReturnReasonEntity content;
     * 3、Channel channel:当前传输数据的通道
     * <p>
     * Queue:可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一个收到此消息场景:
     * 1) 、订单服务启动多个:同一个消息，只能有一个客户端收到
     * 2)、只有一个消息完全处理完,方法运行结束才可以接收下一个消息
     *
     * @param message
     * @param content
     */
    /*@RabbitListener(queues = {"hello.java.queue"})
    public void recieveMessage(Message message, OrderReturnApplyEntity content, Channel channel) {
        log.info("{}", content);
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            if (deliveryTag % 2 == 0) {
                *//**
     * getDeliveryTag()是消息发送的数据顺序标识，依次递增
     * 第二个参数表示是否批量应答（也就是应答一个消息后面的是否都要应答）
     *//*
                channel.basicAck(deliveryTag, false);
            } else {
                *//**
     * 第一个参数：拒收消息的标识
     * 第二个参数：是否批量拒收
     * 第三个参数：
     *  false为拒收之后不放入队列，直接丢弃
     *  true为拒收之后还放入队列，此时服务还可以接收次重新放回的消息
     *//*
                channel.basicNack(deliveryTag, false, false);
                // 少了是否批量拒收的参数
                // channel.basicReject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}