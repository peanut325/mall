package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.atguigu.gulimall.product.vo.BaseAttrs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 10:40:05
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存属性信息和属性分组的关联表
     *
     * @param attrVo
     */
    void saveDetails(AttrVo attrVo);

    /**
     * 查询分页的AttrResVO对象
     *
     * @param params
     * @param catelogId
     * @param attrType
     * @return
     */
    PageUtils queryPageAttrResVo(Map<String, Object> params, Long catelogId, String attrType);

    AttrEntity getAttrInfo(Long attrId);

    void updateAttrVo(AttrVo attrVo);

    /**
     * 分局分组id获取属性
     *
     * @param attrGroupId
     * @return
     */
    List<AttrEntity> getAttrRelation(Long attrGroupId);

    /**
     * 删除属性和分组的关系
     *
     * @param attrGroupRelationVos
     */
    void deleteAttrRelation(AttrGroupRelationVo[] attrGroupRelationVos);

    PageUtils getNoAttrRelationPage(Map<String, Object> params, Long attrGroupId);

    /**
     * 在指定的所有属性和里面，挑出检索属性
     *
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrIds(List<Long> attrIds);
}

