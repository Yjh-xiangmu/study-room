package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.StudyRoom;
import com.example.studyroom.mapper.StudyRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/room")
@CrossOrigin // 允许前端跨域访问
public class StudyRoomController {

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    /**
     * 1. 查询所有自习室门店 (支持按名称模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<StudyRoom>> list(@RequestParam(defaultValue = "") String name) {
        QueryWrapper<StudyRoom> queryWrapper = new QueryWrapper<>();
        if (!name.isEmpty()) {
            queryWrapper.like("name", name); // 如果传了名字，就模糊查询
        }
        queryWrapper.orderByDesc("id"); // 按最新创建的排序
        List<StudyRoom> list = studyRoomMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    /**
     * 2. 新增自习室门店
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody StudyRoom studyRoom) {
        studyRoom.setCreateTime(LocalDateTime.now());
        studyRoomMapper.insert(studyRoom);
        return Result.success("门店添加成功！");
    }

    /**
     * 3. 修改自习室门店信息
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody StudyRoom studyRoom) {
        studyRoomMapper.updateById(studyRoom);
        return Result.success("门店修改成功！");
    }

    /**
     * 4. 删除自习室门店
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        studyRoomMapper.deleteById(id);
        return Result.success("门店删除成功！");
    }
}