package com.example.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roomId;
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualEndTime;
    private Integer status; // 0-已预约待签到, 1-已签到使用中, 2-正常结束, 3-取消, 4-违约
    private BigDecimal totalAmount;
    private LocalDateTime createTime;

    private BigDecimal actualAmount; // 【新增】真实扣费金额
}