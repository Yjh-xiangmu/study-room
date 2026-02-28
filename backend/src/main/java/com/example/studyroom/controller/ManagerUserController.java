package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/user")
@CrossOrigin
public class ManagerUserController {

    @Autowired private SysUserMapper sysUserMapper;

    // 根据手机号查询学生信息
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@RequestParam String phone) {
        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username", phone));
        if (user == null) {
            return Result.error("未找到该手机号对应的学生账户！");
        }
        return Result.success(user);
    }

    // 给学生充值
    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody Map<String, Object> param) {
        String phone = (String) param.get("phone");
        BigDecimal amount = new BigDecimal(param.get("amount").toString());

        SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username", phone));
        if (user == null) return Result.error("未找到该学生账户！");

        user.setBalance(user.getBalance().add(amount));
        sysUserMapper.updateById(user);

        return Result.success("充值成功！该学生当前余额为：" + user.getBalance() + " 元");
    }
}