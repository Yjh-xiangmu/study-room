package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin // 这个注解非常重要！允许前端的 5173 端口跨域访问后端的 8080 端口
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 真实登录接口
     */
    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody SysUser user) {
        // 根据前端传来的 账号、密码、角色 去数据库查询
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername())
                .eq("password", user.getPassword())
                .eq("role", user.getRole());

        SysUser dbUser = sysUserMapper.selectOne(queryWrapper);

        if (dbUser != null) {
            return Result.success(dbUser); // 查到了，登录成功
        } else {
            return Result.error("账号、密码或角色选择错误！");
        }
    }

    /**
     * 真实注册接口
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        // 1. 先校验账号是否已经被注册了
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (sysUserMapper.selectCount(queryWrapper) > 0) {
            return Result.error("该账号已被注册，请换一个账号！");
        }

        // 2. 赋予新用户初始默认值
        user.setRole(1); // 默认注册的都是 1 (考研学生)
        user.setCreditScore(100); // 初始信用分满分100

        // 3. 插入数据库
        sysUserMapper.insert(user);
        return Result.success("注册成功，快去登录吧！");
    }
}