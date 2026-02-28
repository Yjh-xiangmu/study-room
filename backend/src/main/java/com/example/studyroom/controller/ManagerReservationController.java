package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.*;
import com.example.studyroom.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/reservation")
@CrossOrigin
public class ManagerReservationController {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private StudyRoomMapper studyRoomMapper;
    @Autowired private StudyRoomZoneMapper zoneMapper;

    /**
     * 1. 获取有效订单列表 (支持按手机号可选搜索)
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchOrder(@RequestParam(required = false, defaultValue = "") String phone) {

        QueryWrapper<Reservation> resQw = new QueryWrapper<>();
        resQw.in("status", 0, 1) // 始终只查 未核销(0) 和 学习中(1) 的订单
                .orderByDesc("create_time");

        // 如果店长输入了手机号，我们就精准过滤
        if (!phone.isEmpty()) {
            QueryWrapper<SysUser> userQw = new QueryWrapper<>();
            userQw.eq("username", phone).or().eq("phone", phone);
            SysUser user = sysUserMapper.selectOne(userQw);

            if (user == null) {
                // 如果搜不到这个手机号，直接返回空列表给前端
                return Result.success(new ArrayList<>());
            }
            resQw.eq("user_id", user.getId());
        }

        List<Reservation> list = reservationMapper.selectList(resQw);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Reservation r : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());

            // 查出这个订单对应的用户信息
            SysUser u = sysUserMapper.selectById(r.getUserId());
            map.put("nickname", u != null ? u.getNickname() : "未知");
            map.put("phone", u != null ? u.getUsername() : "未知");

            map.put("startTime", r.getStartTime());
            map.put("endTime", r.getEndTime());
            map.put("status", r.getStatus());
            map.put("totalAmount", r.getTotalAmount());

            StudyRoom room = studyRoomMapper.selectById(r.getRoomId());
            map.put("roomName", room != null ? room.getName() : "未知门店");

            Seat seat = seatMapper.selectById(r.getSeatId());
            if (seat != null) {
                StudyRoomZone zone = zoneMapper.selectById(seat.getZoneId());
                map.put("seatInfo", (zone != null ? zone.getName() : "") + " - " + seat.getSeatNo() + "号");
            }
            resultList.add(map);
        }
        return Result.success(resultList);
    }

    /**
     * 2. 确认入座核销 (状态变为 1)
     */
    @PutMapping("/checkin/{id}")
    public Result<String> checkin(@PathVariable Long id) {
        Reservation res = reservationMapper.selectById(id);
        if (res == null || res.getStatus() != 0) {
            return Result.error("订单状态异常，无法核销！");
        }
        res.setStatus(1); // 变为学习中
        res.setStartTime(LocalDateTime.now());
        reservationMapper.updateById(res);
        return Result.success("核销成功！学生已入座 (线下座位已通电)。");
    }
}