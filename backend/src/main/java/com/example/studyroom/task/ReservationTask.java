package com.example.studyroom.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.entity.Reservation;
import com.example.studyroom.entity.Seat;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.ReservationMapper;
import com.example.studyroom.mapper.SeatMapper;
import com.example.studyroom.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationTask {

    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SeatMapper seatMapper;

    /**
     * 定时任务：每分钟执行一次 (cron表达式)
     * 自动处理“超时未到店签到”的预约订单
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional // 开启事务，保证退款、扣分、改座位的原子性
    public void handleExpiredReservations() {
        // 1. 算出30分钟前的时间点
        LocalDateTime thirtyMinsAgo = LocalDateTime.now().minusMinutes(30);

        // 2. 去数据库查：状态为 0 (已预约待签到) 且 创建时间早于30分钟前的订单
        QueryWrapper<Reservation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0)
                .le("create_time", thirtyMinsAgo);

        List<Reservation> expiredList = reservationMapper.selectList(queryWrapper);

        // 3. 遍历这些违约订单，逐一处理
        for (Reservation res : expiredList) {
            System.out.println("检测到超时未签到订单，订单ID: " + res.getId());

            // A. 将订单状态改为 4 (未到违约)
            res.setStatus(4);
            reservationMapper.updateById(res);

            // B. 释放座位：把座位的状态改回 0 (空闲)
            Seat seat = seatMapper.selectById(res.getSeatId());
            if (seat != null) {
                seat.setStatus(0);
                seatMapper.updateById(seat);
            }

            // C. 退还费用并扣除1分信用分
            SysUser user = sysUserMapper.selectById(res.getUserId());
            if (user != null) {
                // 返还金额
                user.setBalance(user.getBalance().add(res.getTotalAmount()));
                // 扣除1分信用分
                user.setCreditScore(user.getCreditScore() - 1);
                sysUserMapper.updateById(user);
            }
        }
    }
}