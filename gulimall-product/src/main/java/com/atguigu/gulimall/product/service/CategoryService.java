package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catalog2VO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 09:55:47
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findCateLogPath(Long catelogId);

    /**
     * 更新分类，同步更新关联表
     *
     * @param category
     */
    void updateDetails(CategoryEntity category);

    /**
     * 获取以及分类
     *
     * @return
     */
    List<CategoryEntity> getLevel1Category();

    /**
     * 前台页面获取分类Json
     *
     * @return
     */
    Map<String, List<Catalog2VO>> getCatalogJson();

    Map<String, List<Catalog2VO>> getCatalogJsonByDbBySpringCache();
}

