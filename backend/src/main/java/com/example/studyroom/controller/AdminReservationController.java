package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.*;
import com.example.studyroom.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reservation")
@CrossOrigin
public class AdminReservationController {

    @Autowired private ReservationMapper reservationMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private StudyRoomMapper studyRoomMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private StudyRoomZoneMapper zoneMapper;

    /**
     * 1. 获取全站所有订单流水 (支持按手机号和状态筛选)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getOrderList(
            @RequestParam(required = false, defaultValue = "") String phone,
            @RequestParam(required = false) Integer status) {

        QueryWrapper<Reservation> qw = new QueryWrapper<>();

        // 1. 如果输入了手机号，先找用户ID
        if (!phone.isEmpty()) {
            SysUser user = sysUserMapper.selectOne(new QueryWrapper<SysUser>().eq("username", phone));
            if (user == null) {
                return Result.success(new ArrayList<>()); // 找不到用户直接返回空
            }
            qw.eq("user_id", user.getId());
        }

        // 2. 如果选择了状态，按状态过滤
        if (status != null) {
            qw.eq("status", status);
        }

        // 按创建时间倒序排，最新的订单在最上面
        qw.orderByDesc("create_time");

        List<Reservation> list = reservationMapper.selectList(qw);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Reservation r : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("startTime", r.getStartTime());
            map.put("endTime", r.getEndTime());
            map.put("actualEndTime", r.getActualEndTime());
            map.put("totalAmount", r.getTotalAmount());
            map.put("status", r.getStatus());
            map.put("createTime", r.getCreateTime());

            // 查学生信息
            SysUser u = sysUserMapper.selectById(r.getUserId());
            map.put("nickname", u != null ? u.getNickname() : "未知");
            map.put("phone", u != null ? u.getUsername() : "未知");

            // 查门店与座位信息
            StudyRoom room = studyRoomMapper.selectById(r.getRoomId());
            map.put("roomName", room != null ? room.getName() : "未知门店");

            Seat seat = seatMapper.selectById(r.getSeatId());
            if (seat != null) {
                StudyRoomZone zone = zoneMapper.selectById(seat.getZoneId());
                map.put("seatInfo", (zone != null ? zone.getName() : "") + " - " + seat.getSeatNo() + "号");
            } else {
                map.put("seatInfo", "未知座位");
            }
            resultList.add(map);
        }

        return Result.success(resultList);
    }

    /**
     * 2. 删除异常订单 (仅限管理员的硬核清理操作)
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        reservationMapper.deleteById(id);
        return Result.success("订单流水记录已彻底删除！");
    }
}