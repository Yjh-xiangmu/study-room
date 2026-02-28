package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Feedback;
import com.example.studyroom.entity.ForumPost;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.FeedbackMapper;
import com.example.studyroom.mapper.ForumPostMapper;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/student/profile")
@CrossOrigin
public class ProfileController {

    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private ForumPostMapper forumPostMapper;
    @Autowired private FeedbackMapper feedbackMapper;

    /**
     * 1. 接收前端文件流，存入数据库二进制字段
     */
    @PostMapping("/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("userId") Long userId, @RequestParam("file") MultipartFile file) {
        try {
            SysUser user = sysUserMapper.selectById(userId);
            user.setAvatar(file.getBytes()); // 转化为 byte[] 存入
            user.setAvatarType(file.getContentType());
            sysUserMapper.updateById(user);
            return Result.success("头像上传成功！");
        } catch (Exception e) {
            return Result.error("头像上传失败！");
        }
    }

    /**
     * 2. 将数据库中的二进制数据还原为图片流返回给前端 `<img src="...">`
     */
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null && user.getAvatar() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(user.getAvatarType()))
                    .body(user.getAvatar());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 3. 修改个人基本资料
     */
    @PutMapping("/update")
    public Result<SysUser> updateProfile(@RequestBody SysUser param) {
        SysUser user = sysUserMapper.selectById(param.getId());
        user.setNickname(param.getNickname());
        if (param.getPassword() != null && !param.getPassword().trim().isEmpty()) {
            user.setPassword(param.getPassword());
        }
        sysUserMapper.updateById(user);

        // 擦除敏感数据后返回给前端更新本地缓存
        user.setPassword(null);
        user.setAvatar(null);
        return Result.success(user);
    }

    /**
     * 4. 获取“我的发帖”列表
     */
    @GetMapping("/myposts/{userId}")
    public Result<List<ForumPost>> getMyPosts(@PathVariable Long userId) {
        QueryWrapper<ForumPost> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("create_time");
        return Result.success(forumPostMapper.selectList(qw));
    }

    /**
     * 5. 提交意见反馈
     */
    @PostMapping("/feedback")
    public Result<String> submitFeedback(@RequestBody Feedback feedback) {
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return Result.success("反馈提交成功，感谢您的建议！");
    }
}