package com.example.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 对应我们数据库里的 sys_user 表
public class SysUser {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Integer role; // 0-管理员, 1-考研学生
    private Integer creditScore;
    private BigDecimal balance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private byte[] avatar;       // 新增：头像二进制数组
    private String avatarType;   // 新增：图片类型 (如 image/jpeg)
}