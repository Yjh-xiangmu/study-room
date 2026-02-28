package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Reservation;
import com.example.studyroom.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/home")
@CrossOrigin
public class ManagerHomeController {

    @Autowired private ReservationMapper reservationMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        // 1. 正在学习的人数 (已被核销入座的)
        long studyingCount = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 1));

        // 2. 待核销人数
        long pendingCount = reservationMapper.selectCount(new QueryWrapper<Reservation>().eq("status", 0));

        // 3. 今日已完成的订单 (今天结束的)
        List<Reservation> todayFinished = reservationMapper.selectList(
                new QueryWrapper<Reservation>().eq("status", 2)
                        .ge("actual_end_time", startOfDay).le("actual_end_time", endOfDay)
        );

        // 4. 计算今日实际到账营收
        BigDecimal todayRevenue = BigDecimal.ZERO;
        for (Reservation r : todayFinished) {
            if (r.getActualAmount() != null) {
                todayRevenue = todayRevenue.add(r.getActualAmount());
            } else if (r.getTotalAmount() != null) {
                todayRevenue = todayRevenue.add(r.getTotalAmount());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("studyingCount", studyingCount);
        map.put("pendingCount", pendingCount);
        map.put("todayOrderCount", todayFinished.size());
        map.put("todayRevenue", todayRevenue);

        return Result.success(map);
    }
}