package com.example.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;         // 所属自习室ID
    private Long zoneId;    // 新增：分区ID
    private String seatNo;  // 新增：具体座位号 (如 01)

      // 0-无插座, 1-有插座
    private Integer status;      // 0-空闲, 1-使用中, 2-维修中
    private LocalDateTime createTime;
}