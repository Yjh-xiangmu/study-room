package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Feedback;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.FeedbackMapper;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/feedback")
@CrossOrigin
public class AdminFeedbackController {

    @Autowired private FeedbackMapper feedbackMapper;
    @Autowired private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        // 按时间倒序查出所有反馈
        List<Feedback> list = feedbackMapper.selectList(new QueryWrapper<Feedback>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Feedback f : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("content", f.getContent());
            map.put("createTime", f.getCreateTime());

            // 附带查出是谁反馈的
            SysUser user = sysUserMapper.selectById(f.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "未知用户");
            map.put("phone", user != null ? user.getUsername() : "未知账号");
            result.add(map);
        }
        return Result.success(result);
    }
}