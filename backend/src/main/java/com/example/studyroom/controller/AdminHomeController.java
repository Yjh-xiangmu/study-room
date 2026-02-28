package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Reservation;
import com.example.studyroom.entity.StudyRoom;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.ReservationMapper;
import com.example.studyroom.mapper.StudyRoomMapper;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/home")
@CrossOrigin
public class AdminHomeController {

    @Autowired private SysUserMapper userMapper;
    @Autowired private ReservationMapper reservationMapper;
    @Autowired private StudyRoomMapper roomMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> data = new HashMap<>();

        // 1. 顶部四个基础统计数据
        long userCount = userMapper.selectCount(new QueryWrapper<SysUser>().eq("role", 1)); // 仅统计学生
        long roomCount = roomMapper.selectCount(null);
        long orderCount = reservationMapper.selectCount(null);

        // 计算总收入 (仅计算状态为2 也就是真实已完成结算的订单)
        List<Reservation> finishedOrders = reservationMapper.selectList(new QueryWrapper<Reservation>().eq("status", 2));
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Reservation r : finishedOrders) {
            if (r.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(r.getTotalAmount());
            }
        }

        data.put("userCount", userCount);
        data.put("roomCount", roomCount);
        data.put("orderCount", orderCount);
        data.put("totalRevenue", totalRevenue);

        // 2. 门店预约热度分布 (给饼图用)
        List<StudyRoom> rooms = roomMapper.selectList(null);
        List<Map<String, Object>> roomChartData = new ArrayList<>();
        for (StudyRoom room : rooms) {
            long count = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("room_id", room.getId()));
            Map<String, Object> map = new HashMap<>();
            map.put("name", room.getName());
            map.put("value", count);
            roomChartData.add(map);
        }
        data.put("roomChartData", roomChartData);

        // 3. 订单状态全景分布 (给柱状图用)
        long status0 = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 0)); // 待核销
        long status1 = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 1)); // 学习中
        long status2 = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 2)); // 已完成
        long status3 = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 3)); // 已取消
        long status4 = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 4)); // 违约

        data.put("statusChartData", Arrays.asList(status0, status1, status2, status3, status4));

        return Result.success(data);
    }
}