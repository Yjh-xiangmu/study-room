package com.example.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyroom.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 继承了 BaseMapper 后，MyBatis-Plus 自动帮我们写好了增删改查的方法！
}