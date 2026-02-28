package com.example.studyroom.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.StudyRoomZone;
import com.example.studyroom.mapper.StudyRoomZoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/admin/zone")
@CrossOrigin
public class StudyRoomZoneController {
    @Autowired private StudyRoomZoneMapper zoneMapper;

    @GetMapping("/list")
    public Result<List<StudyRoomZone>> list(@RequestParam(required = false) Long roomId) {
        QueryWrapper<StudyRoomZone> qw = new QueryWrapper<>();
        if (roomId != null) qw.eq("room_id", roomId);
        return Result.success(zoneMapper.selectList(qw));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody StudyRoomZone zone) {
        zone.setCreateTime(LocalDateTime.now());
        zoneMapper.insert(zone);
        return Result.success("分区添加成功");
    }
    // 新增：编辑分区信息的接口
    @PutMapping("/update")
    public Result<String> update(@RequestBody StudyRoomZone zone) {
        zoneMapper.updateById(zone);
        return Result.success("分区修改成功");
    }
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        zoneMapper.deleteById(id);
        return Result.success("分区删除成功");
    }
    @PostMapping("/image/upload")
    public Result<String> uploadImage(@RequestParam("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            StudyRoomZone zone = zoneMapper.selectById(id);
            if (zone != null) {
                zone.setImage(file.getBytes());
                zone.setImageType(file.getContentType());
                zoneMapper.updateById(zone);
                return Result.success("分区图片上传成功！");
            }
            return Result.error("分区不存在！");
        } catch (Exception e) {
            return Result.error("图片上传失败！");
        }
    }

    /**
     * 6. 获取分区实景图
     */
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        StudyRoomZone zone = zoneMapper.selectById(id);
        if (zone != null && zone.getImage() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(zone.getImageType()))
                    .body(zone.getImage());
        }
        return ResponseEntity.notFound().build();
    }
}