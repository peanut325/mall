package com.atguigu.gulimall.product.vo;

import com.atguigu.common.to.seckill.SeckillSkuRedisTo;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {
    /**
     * 1、sku基本信息【标题、副标题、价格】pms_sku_info
     * 2、sku图片信息【每个sku_id对应了多个图片】pms_sku_images
     * 3、spu下所有sku销售属性组合【不只是当前sku_id所指定的商品】
     * 4、spu商品介绍【】
     * 5、spu规格与包装【参数信息】
     */

    //1、sku基本信息（pms_sku_info）【默认图片、标题、副标题、价格】
    private SkuInfoEntity info;

    private boolean hasStock = true;// 是否有货

    //2、sku图片信息（pms_sku_images）
    private List<SkuImagesEntity> images;

    //3、当前sku所属spu下的所有销售属性组合（pms_sku_sale_attr_value）
    private List<SkuItemSaleAttrVo> saleAttr;

    //4、spu商品介绍（pms_spu_info_desc）【描述图片】
    private SpuInfoDescEntity desc;

    //5、spu规格参数信息（pms_attr）【以组为单位】
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6.当前商品秒杀信息
    private SeckillSkuRedisTo seckillSku;

}