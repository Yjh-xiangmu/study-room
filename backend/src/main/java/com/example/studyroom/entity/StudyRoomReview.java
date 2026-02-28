package com.example.studyroom.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_room_review")
public class StudyRoomReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}