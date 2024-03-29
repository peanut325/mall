package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import org.springframework.util.CollectionUtils;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttrRelationBatch(List<AttrGroupRelationVo> attrGroupRelationVoList) {
        if (!CollectionUtils.isEmpty(attrGroupRelationVoList)) {
            List<AttrAttrgroupRelationEntity> attrGroupRelationEntityList = attrGroupRelationVoList.stream().map(attrGroupRelationVo -> {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                BeanUtils.copyProperties(attrGroupRelationVo, attrAttrgroupRelationEntity);
                return attrAttrgroupRelationEntity;
            }).collect(Collectors.toList());
            this.saveBatch(attrGroupRelationEntityList);
        }
    }

}