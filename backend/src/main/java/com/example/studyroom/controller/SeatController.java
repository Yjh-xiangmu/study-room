package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Seat;
import com.example.studyroom.mapper.SeatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/seat")
@CrossOrigin
public class SeatController {

    @Autowired
    private SeatMapper seatMapper;

    /**
     * 查询座位列表 (可根据门店ID筛选)
     */
    @GetMapping("/list")
    public Result<List<Seat>> list(@RequestParam(required = false) Long roomId) {
        QueryWrapper<Seat> queryWrapper = new QueryWrapper<>();
        if (roomId != null) {
            queryWrapper.eq("room_id", roomId);
        }
        queryWrapper.orderByDesc("id");
        return Result.success(seatMapper.selectList(queryWrapper));
    }

    /**
     * 新增座位
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Seat seat) {
        seat.setCreateTime(LocalDateTime.now());
        seatMapper.insert(seat);
        return Result.success("座位添加成功！");
    }

    /**
     * 修改座位信息 (比如修改状态为报修)
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Seat seat) {
        seatMapper.updateById(seat);
        return Result.success("座位修改成功！");
    }

    /**
     * 删除座位
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        seatMapper.deleteById(id);
        return Result.success("座位删除成功！");
    }
}