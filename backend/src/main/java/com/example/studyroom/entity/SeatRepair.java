package com.example.studyroom.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seat_repair")
public class SeatRepair {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roomId;
    private Long seatId;
    private String content;
    private Integer status; // 0-待处理, 1-维修中, 2-已解决
    private LocalDateTime createTime;
}