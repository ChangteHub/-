package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帮助中心控制器
 */
@Tag(name = "帮助中心接口", description = "帮助中心FAQ")
@RestController
@RequestMapping("/api/help")
public class HelpController {

    @Operation(summary = "获取帮助列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getHelp(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> helpList = Arrays.asList(
            createHelp(1L, "如何发布商品？", "点击底部导航栏的\"发布\"按钮，填写商品信息并上传图片后提交即可。", "发布相关"),
            createHelp(2L, "如何联系卖家？", "在商品详情页点击\"联系卖家\"按钮，即可发起聊天。", "交易相关"),
            createHelp(3L, "如何进行实名认证？", "进入个人中心，点击\"实名认证\"，填写真实信息并上传学生证照片。", "账号相关"),
            createHelp(4L, "如何修改个人信息？", "进入个人中心，点击头像或昵称区域即可编辑个人资料。", "账号相关"),
            createHelp(5L, "商品被下架了怎么办？", "请检查商品信息是否违规，修改后可重新上架。如有疑问请联系客服。", "交易相关"),
            createHelp(6L, "如何收藏商品？", "在商品详情页点击心形图标即可收藏。", "交易相关")
        );

        List<Map<String, Object>> filtered = helpList;
        if (category != null && !category.isEmpty()) {
            filtered = filtered.stream()
                .filter(h -> category.equals(h.get("category")))
                .toList();
        }
        if (keyword != null && !keyword.isEmpty()) {
            filtered = filtered.stream()
                .filter(h -> ((String) h.get("question")).contains(keyword) || ((String) h.get("answer")).contains(keyword))
                .toList();
        }

        return Result.success(filtered);
    }

    private Map<String, Object> createHelp(Long id, String question, String answer, String category) {
        Map<String, Object> help = new HashMap<>();
        help.put("id", id);
        help.put("question", question);
        help.put("answer", answer);
        help.put("category", category);
        return help;
    }
}
