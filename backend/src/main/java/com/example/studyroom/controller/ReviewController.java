package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Reservation;
import com.example.studyroom.entity.StudyRoomReview;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.ReservationMapper;
import com.example.studyroom.mapper.StudyRoomReviewMapper;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/review")
@CrossOrigin
public class ReviewController {

    @Autowired private StudyRoomReviewMapper reviewMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private ReservationMapper reservationMapper;

    /**
     * 1. 获取指定门店的所有评价
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam Long roomId) {
        QueryWrapper<StudyRoomReview> qw = new QueryWrapper<>();
        qw.eq("room_id", roomId).orderByDesc("create_time");
        List<StudyRoomReview> reviews = reviewMapper.selectList(qw);

        List<Map<String, Object>> result = new ArrayList<>();
        for (StudyRoomReview r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("createTime", r.getCreateTime());
            map.put("userId", r.getUserId()); // 传回 userId，方便前端去请求头像

            SysUser user = sysUserMapper.selectById(r.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "匿名用户");
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 2. 检查用户是否有资格评价该门店（必须有 status=2 即“已完成”的订单）
     */
    @GetMapping("/check")
    public Result<Boolean> checkEligibility(@RequestParam Long userId, @RequestParam Long roomId) {
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("room_id", roomId).eq("status", 2);
        boolean hasCompleted = reservationMapper.selectCount(qw) > 0;
        return Result.success(hasCompleted);
    }

    /**
     * 3. 提交评价
     */
    @PostMapping("/add")
    public Result<String> addReview(@RequestBody StudyRoomReview review) {
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);
        return Result.success("评价发布成功！");
    }
}