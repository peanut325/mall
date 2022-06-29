package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.seckill.SeckillSkuRedisTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.client.SeckillFeignService;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class SkuItemServiceImpl implements SkuItemService {

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public SkuItemVo item(String skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            // 1.获取sku基本信息（pms_sku_info）【默认图片、标题、副标题、价格】
            SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        // 2.获取sku图片信息（pms_sku_images）
        List<SkuImagesEntity> imagesEntityList = skuImagesService.getImgBySkuId(skuId);
        skuItemVo.setImages(imagesEntityList);

        CompletableFuture<Void> AttrFuture = skuInfoFuture.thenAcceptAsync(res -> {
            // 3.获取当前sku所属spu下的所有销售属性组合（pms_sku_info、pms_sku_sale_attr_value）
            List<SkuItemSaleAttrVo> skuItemSaleAttrVoList = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVoList);
        }, executor);

        CompletableFuture<Void> spuDescFuture = skuInfoFuture.thenAcceptAsync(res -> {
            // 4.获取spu商品介绍（pms_spu_info_desc）【描述图片】
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> groupFuture = skuInfoFuture.thenAcceptAsync((res -> {
            // 5.获取spu规格参数信息（pms_product_attr_value、pms_attr_attrgroup_relation、pms_attr_group）
            List<SpuItemAttrGroupVo> groupAttrList = attrGroupService.getGroupAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrList);
        }), executor);

        CompletableFuture<Void> seckillFuture = skuInfoFuture.thenAcceptAsync((res -> {
            // 远程调用查询优惠信息
            R r = seckillFeignService.getSkuInfoBySkuId(Long.parseLong(skuId));
            if (r.getCode() == 0) {
                SeckillSkuRedisTo data = r.getData(new TypeReference<SeckillSkuRedisTo>() {
                });
                skuItemVo.setSeckillSku(data);
            }
        }), executor);

        CompletableFuture.allOf(skuInfoFuture, AttrFuture, spuDescFuture, groupFuture, seckillFuture).get();   // 阻塞等待所有执行完成
        return skuItemVo;
    }
}
