package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<List<SysUser>> list(@RequestParam(defaultValue = "") String keyword) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("role", 0); // 不显示超级管理员自己
        if (!keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("username", keyword).or().like("nickname", keyword));
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(sysUserMapper.selectList(queryWrapper));
    }

    // 【新增】管理员手动添加用户接口
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysUser user) {
        // 1. 检查账号是否已存在
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("username", user.getUsername());
        if (sysUserMapper.selectCount(qw) > 0) {
            return Result.error("该账号(手机号)已被注册，请换一个！");
        }

        // 2. 设置初始默认值
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456"); // 默认密码 123456
        }
        user.setPhone(user.getUsername()); // 手机号和账号保持一致
        user.setCreditScore(100);          // 默认信用分满分
        user.setBalance(BigDecimal.ZERO);  // 默认余额 0
        user.setCreateTime(LocalDateTime.now());

        sysUserMapper.insert(user);
        return Result.success("用户添加成功！");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody SysUser user) {
        sysUserMapper.updateById(user);
        return Result.success("用户信息更新成功！");
    }

    @PutMapping("/recharge/{id}")
    public Result<String> recharge(@PathVariable Long id, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0！");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user != null) {
            user.setBalance(user.getBalance().add(amount));
            sysUserMapper.updateById(user);
            return Result.success("充值成功！");
        }
        return Result.error("用户不存在！");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        sysUserMapper.deleteById(id);
        return Result.success("用户删除成功！");
    }
}