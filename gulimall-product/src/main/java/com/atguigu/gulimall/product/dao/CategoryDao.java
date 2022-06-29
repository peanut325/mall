package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 09:55:47
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
