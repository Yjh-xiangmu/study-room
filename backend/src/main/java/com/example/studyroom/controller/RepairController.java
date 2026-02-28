package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.*;
import com.example.studyroom.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repair")
@CrossOrigin
public class RepairController {

    @Autowired private SeatRepairMapper repairMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private StudyRoomMapper roomMapper;
    @Autowired private StudyRoomZoneMapper zoneMapper;
    @Autowired private SysUserMapper userMapper;

    /**
     * 1. 学生提交报修
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody SeatRepair repair) {
        repair.setStatus(0); // 默认为待处理
        repair.setCreateTime(LocalDateTime.now());
        repairMapper.insert(repair);
        return Result.success("报修提交成功，感谢您的反馈！管理人员将尽快处理。");
    }

    /**
     * 2. 获取报修列表 (支持学生查自己的，也支持管理员查所有的)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) Long userId) {
        QueryWrapper<SeatRepair> qw = new QueryWrapper<>();
        if (userId != null) {
            qw.eq("user_id", userId); // 学生只看自己的
        }
        qw.orderByAsc("status").orderByDesc("create_time"); // 待处理的排在最前面

        List<SeatRepair> list = repairMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SeatRepair r : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("content", r.getContent());
            map.put("status", r.getStatus());
            map.put("createTime", r.getCreateTime());

            SysUser user = userMapper.selectById(r.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "未知用户");

            StudyRoom room = roomMapper.selectById(r.getRoomId());
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
     * 3. 管理员开始处理 -> 将座位标为维修中
     */
    @PutMapping("/start/{id}")
    @Transactional
    public Result<String> startRepair(@PathVariable Long id) {
        SeatRepair repair = repairMapper.selectById(id);
        if (repair != null && repair.getStatus() == 0) {
            repair.setStatus(1); // 报修单状态：维修中
            repairMapper.updateById(repair);

            // 联动更新座位状态为 2 (维修中)
            Seat seat = seatMapper.selectById(repair.getSeatId());
            if (seat != null) {
                seat.setStatus(2);
                seatMapper.updateById(seat);
            }
            return Result.success("已接单处理！该座位已被锁定并标记为维修中。");
        }
        return Result.error("操作失败！");
    }

    /**
     * 4. 管理员维修完成 -> 释放座位
     */
    @PutMapping("/finish/{id}")
    @Transactional
    public Result<String> finishRepair(@PathVariable Long id) {
        SeatRepair repair = repairMapper.selectById(id);
        if (repair != null && repair.getStatus() == 1) {
            repair.setStatus(2); // 报修单状态：已解决
            repairMapper.updateById(repair);

            // 联动恢复座位状态为 0 (空闲)
            Seat seat = seatMapper.selectById(repair.getSeatId());
            if (seat != null) {
                seat.setStatus(0);
                seatMapper.updateById(seat);
            }
            return Result.success("维修完成！该座位已恢复空闲，可供重新预约。");
        }
        return Result.error("操作失败！");
    }
}