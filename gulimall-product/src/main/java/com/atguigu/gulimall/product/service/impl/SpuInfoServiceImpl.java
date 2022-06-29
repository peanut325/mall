package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.product.SpuConstant;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.to.order.SpuInfoTo;
import com.atguigu.common.to.product.SkuReductionTo;
import com.atguigu.common.to.product.SpuBoundTo;
import com.atguigu.common.to.ware.SkuHasStockTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.client.CouponFeignService;
import com.atguigu.gulimall.product.client.SearchFeignService;
import com.atguigu.gulimall.product.client.WareFeignService;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 高级部分完善
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        productAttrValueService.saveAttrBatch(spuInfoEntity.getId(), baseAttrs);

        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r1.getCode() != 0) {
            log.error("远程调用保存积分信息失败！");
        }

        //5、保存当前spu对应的所有sku信息；
        List<Skus> skus = spuSaveVo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {   // 判断非空
            skus.forEach(sku -> {
                // 获取是否是默认图片
                List<Images> imagesList = sku.getImages();
                String defaultImageUrl = "";
                for (Images image : imagesList) {
                    if (image.getDefaultImg() == 1) {
                        defaultImageUrl = image.getImgUrl();
                    }
                }

                //5.1）、sku的基本信息；pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setSkuDefaultImg(defaultImageUrl);
                skuInfoEntity.setSaleCount(0L);
                skuInfoService.save(skuInfoEntity);

                //5.2）、sku的图片信息；pms_sku_image
                List<SkuImagesEntity> imagesEntityList = imagesList.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId()); // 需要先保存Sku的信息才会有id
                    return skuImagesEntity;
                }).filter(image -> !StringUtils.isEmpty(image.getImgUrl())) // 过滤，为空不保存
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntityList);   // 批量保存

                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrList = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrList.stream().map(attr -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, attrValueEntity);
                    attrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return attrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);

                //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());  // 获取上一步的skuId，进行保存
                // 有满减或者打折优惠才保存
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r2.getCode() != 0) {
                        log.error("远程调用保存优惠，减满信息失败！");
                    }
                }
            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("sku_name", key);
            });
            // 注意要使用and拼接，如果不适用and会造成 status = 1 and id = 'key' or sku_name = 'key'
            // 使用and会生成括号status = 1 and ( id = 'key' or sku_name = 'key' )
        }

        String catelogId = (String) params.get("catelogId");
        // 分类不为0才封装
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(brandId)) {
            queryWrapper.eq("publish_status", status);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 先查出spu对应的sku信息
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.getSkuBySpuId(spuId);

        // 获取所有的skuId
        List<Long> skuIdList = skuInfoEntityList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 设置属性
        List<ProductAttrValueEntity> attrValueEntityList = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = attrValueEntityList.stream().map(attrValueEntity -> attrValueEntity.getAttrId()).collect(Collectors.toList());

        List<Long> attrSearchIds = attrService.selectSearchAttrIds(attrIds);    // 查出满足检索条件的属性

        HashSet<Long> set = new HashSet<>(attrSearchIds);

        // 封装成搜索的属性
        List<SkuEsModel.Attrs> searchAttrs = attrValueEntityList.stream().filter(attrValueEntity -> set.contains(attrValueEntity.getAttrId())).map(attrValueEntity -> {
            SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(attrValueEntity, attr);
            return attr;
        }).collect(Collectors.toList());

        // 远程调用查询库存信息
        Map<Long, Boolean> stockMap = new HashMap<>();
        try {
            R r = wareFeignService.getSkuHasStock(skuIdList);
            // TypeReference构造器失手保护，需要以内部类构造
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {
            };
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, skuHasStockTo -> skuHasStockTo.getHasStock()));
        } catch (Exception e) {
            log.error("远程调用出现异常:" + e);
        }

        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModelList = skuInfoEntityList.stream().map(skuInfo -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            // 将一些属性进行对拷
            BeanUtils.copyProperties(skuInfo, skuEsModel);
            // 设置不对应的属性值
            skuEsModel.setSkuPrice(skuInfo.getPrice());
            skuEsModel.setSkuImg(skuInfo.getSkuDefaultImg());

            // 查询品牌信息进行封装
            BrandEntity brandById = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandId(brandById.getBrandId());
            skuEsModel.setBrandName(brandById.getName());
            skuEsModel.setBrandImg(brandById.getLogo());

            // TODO 热度评分，先默认设置为0
            skuEsModel.setHotScore(0L);

            // 设置库存信息
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(skuEsModel.getSkuId()));
            }

            // 查出分类信息进行封装
            CategoryEntity categoryEntityById = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntityById.getName());

            // 设置检索属性值
            skuEsModel.setAttrs(searchAttrs);


            return skuEsModel;
        }).collect(Collectors.toList());

        // 将数据保存到ES
        R r = searchFeignService.productStatusUp(skuEsModelList);
        if (r.getCode() == 0) {
            // 远程调用成功，修改当前spu状态
            baseMapper.updateSpuUpStatus(spuId, SpuConstant.PublishStatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用问题（接口幂等性）
        }

    }

    @Override
    public SpuInfoEntity getSpuBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfoEntity = baseMapper.selectById(byId.getSpuId());
        return spuInfoEntity;
    }

}