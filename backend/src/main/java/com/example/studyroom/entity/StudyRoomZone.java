package com.example.studyroom.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Data
@TableName("study_room_zone")
public class StudyRoomZone {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String name;
    private LocalDateTime createTime;
    private String facilities;
    private BigDecimal hourlyPrice;
}