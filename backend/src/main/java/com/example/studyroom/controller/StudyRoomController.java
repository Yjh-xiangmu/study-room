package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.StudyRoom;
import com.example.studyroom.mapper.StudyRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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
    @PostMapping("/cover/upload")
    public Result<String> uploadCover(@RequestParam("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            StudyRoom room = studyRoomMapper.selectById(id);
            if (room != null) {
                room.setCover(file.getBytes());
                room.setCoverType(file.getContentType());
                studyRoomMapper.updateById(room);
                return Result.success("门店封面图上传成功！");
            }
            return Result.error("门店不存在！");
        } catch (Exception e) {
            return Result.error("封面图上传失败！");
        }
    }

    /**
     * 6. 获取门店封面图
     */
    @GetMapping("/cover/{id}")
    public ResponseEntity<byte[]> getCover(@PathVariable Long id) {
        StudyRoom room = studyRoomMapper.selectById(id);
        if (room != null && room.getCover() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(room.getCoverType()))
                    .body(room.getCover());
        }
        return ResponseEntity.notFound().build();
    }
}