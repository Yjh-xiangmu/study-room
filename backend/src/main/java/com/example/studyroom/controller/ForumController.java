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
@RequestMapping("/api/forum")
@CrossOrigin
public class ForumController {

    @Autowired private ForumPostMapper postMapper;
    @Autowired private ForumCommentMapper commentMapper;
    @Autowired private SysUserMapper sysUserMapper;

    /**
     * 1. 获取交流板列表 (包含帖子、发帖人昵称、以及底下的所有评论)
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        // 按时间倒序查出所有帖子
        List<ForumPost> posts = postMapper.selectList(new QueryWrapper<ForumPost>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();

        for (ForumPost post : posts) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("content", post.getContent());
            postMap.put("createTime", post.getCreateTime());

            // 查发帖人昵称
            SysUser user = sysUserMapper.selectById(post.getUserId());
            postMap.put("nickname", user != null ? user.getNickname() : "匿名研友");

            // 查这个帖子下的所有评论
            List<ForumComment> comments = commentMapper.selectList(
                    new QueryWrapper<ForumComment>().eq("post_id", post.getId()).orderByAsc("create_time")
            );
            List<Map<String, Object>> commentList = new ArrayList<>();
            for (ForumComment c : comments) {
                Map<String, Object> cMap = new HashMap<>();
                cMap.put("id", c.getId());
                cMap.put("content", c.getContent());
                SysUser cUser = sysUserMapper.selectById(c.getUserId());
                cMap.put("nickname", cUser != null ? cUser.getNickname() : "匿名");
                commentList.add(cMap);
            }
            postMap.put("comments", commentList);

            result.add(postMap);
        }
        return Result.success(result);
    }

    /**
     * 2. 发布新帖子
     */
    @PostMapping("/post/add")
    public Result<String> addPost(@RequestBody ForumPost post) {
        post.setCreateTime(LocalDateTime.now());
        postMapper.insert(post);
        return Result.success("发布成功！");
    }

    /**
     * 3. 发表评论
     */
    @PostMapping("/comment/add")
    public Result<String> addComment(@RequestBody ForumComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return Result.success("评论成功！");
    }

    /**
     * 4. 删除帖子 (管理员审核用，或者用户删自己的)
     */
    @DeleteMapping("/post/delete/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        // 删除帖子前，先把这个帖子底下的评论全删了，防止产生垃圾数据
        commentMapper.delete(new QueryWrapper<ForumComment>().eq("post_id", id));
        postMapper.deleteById(id);
        return Result.success("帖子及相关评论已删除！");
    }
}