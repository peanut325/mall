package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.product.AttrConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.atguigu.gulimall.product.vo.BaseAttrs;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveDetails(AttrVo attrVo) {
        // 保存属性信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);

        // 是基本属性才进行保存
        if (attrVo.getAttrType() == AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null) {
            // 更新关联信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryPageAttrResVo(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : AttrConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        // 封装查询条件
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        List<AttrEntity> attrEntityList = page.getRecords();

        // 使用流的方式来循环进行查库，封装完成AttrResVo对象
        List<AttrRespVo> attrResVoList = attrEntityList.stream().map(attrEntity -> {
            AttrRespVo attrResVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrResVo);

            // 如果是基本属性才查询分组
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity attrGroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrGroupRelationEntity != null && attrGroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity groupEntity = attrGroupService.getById(attrGroupRelationEntity.getAttrGroupId());
                    attrResVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResVo.setCatelogName(categoryEntity.getName());
            }

            return attrResVo;
        }).collect(Collectors.toList());

        // 返回处理好后的结果集
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrResVoList);

        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrInfo:' + #root.args[0]")
    @Override
    public AttrEntity getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrResVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrResVo);

        if (attrEntity.getAttrType() == AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 查询设置分组ID和分组名
            AttrAttrgroupRelationEntity attrGroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrGroupRelationEntity != null) {
                attrResVo.setAttrGroupId(attrGroupRelationEntity.getAttrGroupId());
                AttrGroupEntity groupEntity = attrGroupService.getById(attrGroupRelationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    attrResVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }

        // 设置路径
        Long[] cateLogPath = categoryService.findCateLogPath(attrEntity.getCatelogId());
        attrResVo.setCatelogPath(cateLogPath);

        // 设置分类名
        CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrResVo.setCatelogName(categoryEntity.getName());
        }

        return attrResVo;
    }

    @Override
    @Transactional
    public void updateAttrVo(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.updateById(attrEntity);

        if (attrVo.getAttrType() == AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 修改分组关联
            AttrAttrgroupRelationEntity attGroupRelationEntity = new AttrAttrgroupRelationEntity();
            attGroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attGroupRelationEntity.setAttrId(attrVo.getAttrId());

            int count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(attGroupRelationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attGroupRelationEntity);
            }
        }

    }

    @Override
    public List<AttrEntity> getAttrRelation(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> entityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIdList = entityList.stream().map(entity -> {
            Long attrId = entity.getAttrId();
            return attrId;
        }).collect(Collectors.toList());

        // 非空判断
        if (attrIdList == null || attrIdList.size() == 0) {
            return null;
        }

        List<AttrEntity> attrEntityList = baseMapper.selectBatchIds(attrIdList);
        return attrEntityList;
    }

    @Override
    public void deleteAttrRelation(AttrGroupRelationVo[] attrGroupRelationVos) {
        List<AttrAttrgroupRelationEntity> entityList = Arrays.stream(attrGroupRelationVos).map(attrGroupRelationVo -> {
            AttrAttrgroupRelationEntity attrGroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(attrGroupRelationVo, attrGroupRelationEntity);
            return attrGroupRelationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(entityList);
    }

    @Override
    public PageUtils getNoAttrRelationPage(Map<String, Object> params, Long attrGroupId) {
        // 现在分组的表中查出分类的id
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 只能显示当前分类下没有被关联的属性
        // 查找当前分类下的其他分组
        List<AttrGroupEntity> attrGroupEntityList = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> groupIds = attrGroupEntityList.stream().map(entity -> {
            Long groupId = entity.getAttrGroupId();
            return groupId;
        }).collect(Collectors.toList());

        // 查出这些分组已关联的属性
        List<AttrAttrgroupRelationEntity> relationEntities = new ArrayList<>();
        if (!CollectionUtils.isEmpty(groupIds)) {
            relationEntities = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        }
        List<Long> attrIds = relationEntities.stream().map(entity -> {
            Long attrId = entity.getAttrId();
            return attrId;
        }).collect(Collectors.toList());

        // 从当前分类下的所有属性里 剔除已关联的属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", AttrConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (!CollectionUtils.isEmpty(attrIds)) {
            queryWrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        List<Long> longList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(attrIds)) {
            longList = attrDao.selectSearchAttrIds(attrIds);
        }
        return longList;
    }

}