package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.to.order.OrderTo;
import com.atguigu.common.to.ware.SkuHasStockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.client.OrderFeignService;
import com.atguigu.gulimall.ware.client.ProductFeignService;
import com.atguigu.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.atguigu.gulimall.ware.entity.SkuWareHasStock;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        int count = this.count(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        // 新增查找
        if (count == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku的名字
            // 失败无需回滚：1.自己catch掉异常 2.TODO 解决方法在高级篇
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception exception) {
            }
            this.save(wareSkuEntity);
        } else { // 更新操作
            wareSkuDao.updateStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> hasStockToList = skuIds.stream().map(skuId -> {
            SkuHasStockTo skuHasStockTo = new SkuHasStockTo();
            Long count = wareSkuDao.getSkuStock(skuId);
            skuHasStockTo.setHasStock(count == null ? false : count > 0);
            skuHasStockTo.setSkuId(skuId);
            return skuHasStockTo;
        }).collect(Collectors.toList());

        return hasStockToList;
    }

    /**
     * 锁定库存,sql执行锁定锁定
     *
     * @param wareSkuLockVo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean lockOrderStock(WareSkuLockVo wareSkuLockVo) {
        // 保存库存工作单的详情
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 按照收货地址找到就近仓库，锁定库存（暂未实现）
        // 采用方案：获取每项商品在哪些仓库有库存，轮询尝试锁定，任一商品锁定失败回滚

        // 找到这个商品在哪里有库存，封装成一个对象
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> skuWareHasStockList = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            List<Long> wareIds = wareSkuDao.getSkuStockWareIds(skuId, item.getCount());
            // 没有仓库
            if (wareIds == null && wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            skuWareHasStock.setLockNum(item.getCount());
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock skuWareHasStock : skuWareHasStockList) {
            // 是否锁定成功的标志
            Boolean skuStockLock = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();

            if (CollectionUtils.isEmpty(wareIds)) {
                // 只要有一个货物没有仓库有他库存，直接抛出异常，锁定库存失败，回滚
                throw new NoStockException(skuId);
            } else {
                // 有多个仓库有库存，此时需要判断哪个仓库够
                for (Long wareId : wareIds) {
                    // 锁定成功就返回1，不成功返回0（代表影响的行数）
                    Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHasStock.getLockNum());
                    if (count == 1) {
                        // 表示已经有一个仓库锁住了
                        // 标志置为true，停止锁下一个仓库
                        skuStockLock = true;

                        // 保存在锁定的信息表中
                        Long taskId = wareOrderTaskEntity.getId();
                        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", skuWareHasStock.getLockNum(), taskId, wareId, 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                        // 告诉MQ库存锁定成功
                        // 锁定失败：前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到id，所以就不用解锁
                        // 为什么不建议向消息队列直接发送任务id呢？
                        //  这里是本地事务，锁库存失败，ware的lock字段和任务详情都会回滚
                        //  加入锁库是其他微服务调用，此时锁库存失败，那么只有任务详情回滚，
                        //  ware不回滚，此时就无法知道锁了多少库存，回滚多少，因为任务详情回滚了，所以保险起见，应该传入entity对象
                        StockLockedTo stockLockedTo = new StockLockedTo();
                        stockLockedTo.setId(taskId);
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                        stockLockedTo.setDetail(stockDetailTo);

                        // 发送消息
                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                        break;
                    }

                    // 当前仓库失败，重试下一个仓库

                    // 所有仓库都没成功锁住，抛出异常
                    if (skuStockLock == false) {
                        throw new NoStockException(skuId);
                    }
                }
            }
        }
        return true;
    }

    public void handleStockLockedRelease(StockLockedTo stockLockedTo) throws Exception {
        /**
         * 回滚消息的逻辑：
         *  一.先在任务详情表中查询消息:
         *    1.如果没有，那么说明在锁定库存的时候已经出现了错误，在本地事务已经进行了回滚，所以所以此时不需要回滚
         *    2.如果有：
         *      解锁需要判断订单情况：
         *          如果没有这个订单，必须解锁。
         *          如果有这个订单：1.订单未取消，不解锁 2.订单取消了，解锁
         */
        Long id = stockLockedTo.getId();
        StockDetailTo stockDetailTo = stockLockedTo.getDetail();
        WareOrderTaskDetailEntity taskDetail = wareOrderTaskDetailService.getById(stockDetailTo.getId());
        if (taskDetail != null) {
            // 此时远程调用查询订单情况
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            R r = orderFeignService.getOrderStatus(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });
                // 只有不存在订单和订单一已经关闭才解锁
                if (data == null || data.getStatus() == 4) {
                    // 当任务详情表中的状态为1，也就是未解锁的时候才进行解锁
                    if (taskDetail.getLockStatus() == 1) {
                        // 调用解锁方法
                        unLockStock(taskDetail.getSkuId(), taskDetail.getWareId(), taskDetail.getSkuNum(), taskDetail.getId());
                    } else {
                        // 订单其他状态，不可解锁（消息确认）
                    }
                }
            } else {
                // 订单远程调用失败（消息重新入队）
                throw new RuntimeException("解锁异常");
            }
        } else {
            // 无库存锁定工作单记录，已回滚，无需解锁（消息确认）
        }
    }

    @Override
    @Transactional
    public void handleStockLockedReleaseByOrderSend(OrderTo orderTo) {
        // 通过任务表查询任务id
        String orderSn = orderTo.getOrderSn();
        Long taskId = orderTaskService.getTaskIdByOrderSn(orderSn);

        // 查询所有任务详情的信息，注意找的是没有解锁的任务详情
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId)
                .eq("lock_status", 1));

        // 遍历解锁库存
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    /**
     * 解锁库存
     *
     * @param skuId
     * @param wareId
     * @param num
     * @param taskId
     */
    @Transactional
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskId) {
        // 仓库解锁
        wareSkuDao.unLockStock(skuId, wareId, num);
        // 更新解锁任务单详情表的解锁状态
        WareOrderTaskDetailEntity detail = new WareOrderTaskDetailEntity();
        detail.setId(taskId);
        detail.setLockStatus(2);
        wareOrderTaskDetailService.updateById(detail);
    }

}