package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author peanut
 * @email peanut@gmail.com
 * @date 2022-04-20 12:38:51
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
