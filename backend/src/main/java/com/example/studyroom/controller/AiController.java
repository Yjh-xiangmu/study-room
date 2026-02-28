package com.example.studyroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.studyroom.common.Result;
import com.example.studyroom.entity.Reservation;
import com.example.studyroom.entity.SysUser;
import com.example.studyroom.mapper.ReservationMapper;
import com.example.studyroom.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    private final String DEEPSEEK_API_KEY = "sk-9ca6c1e26ce740dcbd3e01b3c39109b4";
    private final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";

    @Autowired private SysUserMapper userMapper;
    @Autowired private ReservationMapper reservationMapper;

    // 设置请求超时时间，防卡死
    private RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(60000);    // 60秒等待超时
        return new RestTemplate(factory);
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> param) {
        String userMessage = param.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }

        String systemPrompt = "你是一个考研自习室的专属AI伴学助理，名叫'研途小助手'。你的性格既像严谨的辅导老师，能解答学习问题；又像温柔的学长学姐，能倾听学生的压力并提供心理疏导。你的回答要专业、温暖、排版清晰。请严格使用标准Markdown语法。注意：在解答数学公式时，请直接使用基础文本表达（例如 x^2，F(x)），绝对不要使用 \\[ \\] 或 $$ 等 LaTeX 复杂公式语法，以免网页无法显示。";
        String response = callDeepSeek(systemPrompt, userMessage);
        return Result.success(response);
    }

    @GetMapping("/report")
    public Result<String> generateReport(@RequestParam Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        List<Reservation> finishedOrders = reservationMapper.selectList(
                new QueryWrapper<Reservation>().eq("user_id", userId).eq("status", 2));

        long totalMinutes = 0;
        for (Reservation r : finishedOrders) {
            if (r.getStartTime() != null && r.getActualEndTime() != null) {
                totalMinutes += Duration.between(r.getStartTime(), r.getActualEndTime()).toMinutes();
            }
        }
        double totalHours = Math.round((totalMinutes / 60.0) * 10) / 10.0;
        int orderCount = finishedOrders.size();

        long breakRulesCount = reservationMapper.selectCount(
                new QueryWrapper<Reservation>().eq("user_id", userId).eq("status", 4));

        int creditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;

        String dataPrompt = String.format(
                "请根据以下学生的真实自习室数据，生成一份不超过300字的【专属学情分析报告与鼓励语】。语气要像导师一样专业且充满激励。" +
                        "\n学生昵称：%s" +
                        "\n累计完成学习打卡：%d 次" +
                        "\n累计学习总时长：%.1f 小时" +
                        "\n当前信用积分：%d 分" +
                        "\n超时违约次数：%d 次",
                user.getNickname(), orderCount, totalHours, creditScore, breakRulesCount
        );

        String systemPrompt = "你是自习室首席数据分析师兼考研激励导师。你会根据用户数据生成严谨客观但极具情绪价值的报告。";

        String response = callDeepSeek(systemPrompt, dataPrompt);
        return Result.success(response);
    }

    /**
     * 核心调用逻辑：暴力提取纯文本，杜绝一切隐形报错
     */
    private String callDeepSeek(String systemContent, String userContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(DEEPSEEK_API_KEY);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemContent);
            messages.add(sysMsg);

            Map<String, String> usrMsg = new HashMap<>();
            usrMsg.put("role", "user");
            usrMsg.put("content", userContent);
            messages.add(usrMsg);

            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = getRestTemplate();

            // 【核心改动】直接把返回值当成纯文本接收
            ResponseEntity<String> response = restTemplate.postForEntity(DEEPSEEK_URL, entity, String.class);
            String rawJson = response.getBody();

            // 把 DeepSeek 的原话打印在你的 IDEA 控制台里！
            System.out.println("====== DeepSeek 原生返回数据 ======");
            System.out.println(rawJson);
            System.out.println("===================================");

            // 用强大的 ObjectMapper 提取文本
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);

            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                return root.get("choices").get(0).path("message").path("content").asText();
            } else {
                return "解析失败，DeepSeek返回了：\n" + rawJson;
            }

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("❌ API报错: " + e.getRawStatusCode() + " - " + e.getResponseBodyAsString());
            return "⚠️ API请求失败：错误码 " + e.getRawStatusCode() + "，信息：" + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("❌ 内部异常: " + e.getMessage());
            return "⚠️ 请求过程中发生内部错误：" + e.getMessage();
        }
    }
}