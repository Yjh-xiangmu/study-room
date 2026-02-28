package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.*;
import com.example.studyroom.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/reservation")
@CrossOrigin
public class ReservationController {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private StudyRoomMapper studyRoomMapper;
    @Autowired private StudyRoomZoneMapper zoneMapper;

    /**
     * 1. 提交预约申请并扣费
     */
    @PostMapping("/book")
    @Transactional
    public Result<String> book(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long roomId = Long.valueOf(payload.get("roomId").toString());
        Long seatId = Long.valueOf(payload.get("seatId").toString());
        Double duration = Double.valueOf(payload.get("duration").toString());

        SysUser user = sysUserMapper.selectById(userId);
        Seat seat = seatMapper.selectById(seatId);

        if (seat == null || seat.getStatus() != 0) {
            return Result.error("该座位已被预约或正在维修！");
        }

        StudyRoomZone zone = zoneMapper.selectById(seat.getZoneId());
        BigDecimal totalAmount = zone.getHourlyPrice().multiply(new BigDecimal(duration));

        if (user.getBalance().compareTo(totalAmount) < 0) {
            return Result.error("余额不足，请联系管理员充值！");
        }

        // 扣费
        user.setBalance(user.getBalance().subtract(totalAmount));
        sysUserMapper.updateById(user);

        // 锁定座位
        seat.setStatus(1);
        seatMapper.updateById(seat);

        // 创建订单
        Reservation res = new Reservation();
        res.setUserId(userId);
        res.setRoomId(roomId);
        res.setSeatId(seatId);
        res.setTotalAmount(totalAmount);
        // 初始状态下，如果完整履约，实际扣费等于预付金额
        res.setActualAmount(totalAmount);

        LocalDateTime now = LocalDateTime.now();
        res.setStartTime(now);
        // 预估结束时间 = 当前时间 + 预约小时数
        res.setEndTime(now.plusMinutes((long)(duration * 60)));
        res.setStatus(0); // 待到店核销
        res.setCreateTime(now);

        reservationMapper.insert(res);
        return Result.success("预约成功！已扣除 ￥" + totalAmount);
    }

    /**
     * 2. 获取学生的预约列表 (包含实扣金额展示)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam Long userId) {
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        List<Reservation> list = reservationMapper.selectList(qw);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Reservation r : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("startTime", r.getStartTime());
            map.put("endTime", r.getEndTime());
            map.put("totalAmount", r.getTotalAmount());
            // 如果 actualAmount 不为空则用它，否则用 totalAmount
            map.put("actualAmount", r.getActualAmount() != null ? r.getActualAmount() : r.getTotalAmount());
            map.put("status", r.getStatus());
            map.put("createTime", r.getCreateTime());

            StudyRoom room = studyRoomMapper.selectById(r.getRoomId());
            map.put("roomName", room != null ? room.getName() : "未知门店");

            Seat seat = seatMapper.selectById(r.getSeatId());
            if (seat != null) {
                StudyRoomZone zone = zoneMapper.selectById(seat.getZoneId());
                map.put("seatInfo", (zone != null ? zone.getName() : "") + " - " + seat.getSeatNo() + "号");
            }
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 3. 取消预约 (15分钟内全额退款，超时扣信用分)
     */
    @PutMapping("/cancel/{id}")
    @Transactional
    public Result<String> cancel(@PathVariable Long id) {
        Reservation res = reservationMapper.selectById(id);
        if (res == null || res.getStatus() != 0) {
            return Result.error("订单状态异常，无法取消！");
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = Duration.between(res.getCreateTime(), now).toMinutes();

        SysUser user = sysUserMapper.selectById(res.getUserId());
        String msg = "取消成功！全额退款已退回钱包。";

        // 超时取消，扣除信用分
        if (minutesDiff > 15) {
            user.setCreditScore(user.getCreditScore() - 1);
            msg = "取消成功！因超过15分钟，已扣除1点信用分，全额退款已退回钱包。";
        }

        // 退款并将真实扣费设为0
        user.setBalance(user.getBalance().add(res.getTotalAmount()));
        sysUserMapper.updateById(user);

        res.setStatus(3); // 已取消
        res.setActualAmount(BigDecimal.ZERO); // 取消订单，真实扣费为 0
        reservationMapper.updateById(res);

        // 释放座位
        Seat seat = seatMapper.selectById(res.getSeatId());
        if (seat != null) {
            seat.setStatus(0);
            seatMapper.updateById(seat);
        }

        return Result.success(msg);
    }

    /**
     * 4. 提前离座与结算 (硬核商业计费：按未开始的整小时退费)
     */
    @PutMapping("/checkout/{id}")
    @Transactional
    public Result<String> checkout(@PathVariable Long id) {
        Reservation res = reservationMapper.selectById(id);
        if (res == null || res.getStatus() != 1) {
            return Result.error("只有在学习中的订单才能进行离座结算！");
        }

        LocalDateTime now = LocalDateTime.now();
        res.setActualEndTime(now);
        res.setStatus(2); // 正常结束

        long actualMinutes = Duration.between(res.getStartTime(), now).toMinutes();
        long bookedMinutes = Duration.between(res.getStartTime(), res.getEndTime()).toMinutes();

        BigDecimal refundAmount = BigDecimal.ZERO;
        long refundHours = 0;

        // 如果提前离开
        if (actualMinutes < bookedMinutes) {
            long unusedMinutes = bookedMinutes - actualMinutes;
            refundHours = unusedMinutes / 60; // 只退未开始的完整小时

            if (refundHours > 0) {
                Seat seat = seatMapper.selectById(res.getSeatId());
                StudyRoomZone zone = zoneMapper.selectById(seat.getZoneId());
                BigDecimal hourlyPrice = zone.getHourlyPrice();

                refundAmount = hourlyPrice.multiply(new BigDecimal(refundHours));

                SysUser user = sysUserMapper.selectById(res.getUserId());
                if (user != null) {
                    user.setBalance(user.getBalance().add(refundAmount));
                    sysUserMapper.updateById(user);
                }
            }
        }

        // 计算真实扣费并更新
        res.setActualAmount(res.getTotalAmount().subtract(refundAmount));
        reservationMapper.updateById(res);

        // 释放座位
        Seat seat = seatMapper.selectById(res.getSeatId());
        if (seat != null) {
            seat.setStatus(0);
            seatMapper.updateById(seat);
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            return Result.success("离座成功！已为您退还 " + refundHours + " 个未开始小时的费用：￥" + refundAmount);
        } else {
            return Result.success("离座成功！剩余时间不足1小时，不予退费。座位已释放。");
        }
    }

    /**
     * 5. 首页真实数据统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestParam Long userId) {
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("status", 2);
        List<Reservation> myOrders = reservationMapper.selectList(qw);

        long myTotalMinutes = 0;
        for (Reservation r : myOrders) {
            if (r.getStartTime() != null && r.getActualEndTime() != null) {
                myTotalMinutes += Duration.between(r.getStartTime(), r.getActualEndTime()).toMinutes();
            }
        }
        double myTotalHours = Math.round((myTotalMinutes / 60.0) * 10) / 10.0;

        List<Reservation> allOrders = reservationMapper.selectList(new QueryWrapper<Reservation>().eq("status", 2));
        Map<Long, Long> userMinutesMap = new HashMap<>();
        for (Reservation r : allOrders) {
            if (r.getStartTime() != null && r.getActualEndTime() != null) {
                long mins = Duration.between(r.getStartTime(), r.getActualEndTime()).toMinutes();
                userMinutesMap.put(r.getUserId(), userMinutesMap.getOrDefault(r.getUserId(), 0L) + mins);
            }
        }

        int totalUsers = userMinutesMap.size();
        int beatCount = 0;
        for (long mins : userMinutesMap.values()) {
            if (myTotalMinutes > mins) beatCount++;
        }

        int beatPercentage = 0;
        if (totalUsers > 1) {
            beatPercentage = (int) ((beatCount * 100.0) / (totalUsers - 1));
        } else if (totalUsers == 1 && myTotalMinutes > 0) {
            beatPercentage = 99;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("totalHours", myTotalHours);
        map.put("totalCount", myOrders.size());
        map.put("beatPercentage", beatPercentage);

        return Result.success(map);
    }
}