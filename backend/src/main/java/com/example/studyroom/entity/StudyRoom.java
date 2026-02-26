package com.example.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("study_room")
public class StudyRoom {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;        // 门店名称
    private String location;    // 物理位置
    private LocalTime openTime; // 营业开始时间 (使用 LocalTime 对应数据库的 time)
    private LocalTime closeTime;// 营业结束时间
    private String description; // 简介与配套
    private Integer status;     // 状态: 0-停业, 1-正常营业
    private LocalDateTime createTime; // 创建时间
}