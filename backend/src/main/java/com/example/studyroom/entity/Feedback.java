package com.example.studyroom.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
}