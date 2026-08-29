package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.dto.request.VerificationRequest;
import com.xust.secondhand.entity.Verification;
import com.xust.secondhand.repository.VerificationRepository;
import com.xust.secondhand.service.VerificationService;
import com.xust.secondhand.dto.response.VerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationRepository verificationRepository;

    @Override
    public void submit(Long userId, VerificationRequest dto) {
        // 检查是否已有待审核或已通过的记录
        LambdaQueryWrapper<Verification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Verification::getUserId, userId)
               .in(Verification::getStatus, 0, 1);
        if (verificationRepository.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("已有认证记录，请勿重复提交");
        }

        Verification verification = new Verification();
        verification.setUserId(userId);
        verification.setRealName(dto.getRealName());
        verification.setStudentId(dto.getStudentId());
        verification.setCollege(dto.getCollege());
        verification.setEnrollYear(dto.getEnrollYear());
        verification.setStudentCardUrl(dto.getStudentCardUrl());
        verification.setStatus(0);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        verificationRepository.insert(verification);
    }

    @Override
    public VerificationResponse getStatus(Long userId) {
        LambdaQueryWrapper<Verification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Verification::getUserId, userId)
               .orderByDesc(Verification::getCreatedAt)
               .last("LIMIT 1");
        Verification verification = verificationRepository.selectOne(wrapper);

        if (verification == null) {
            VerificationResponse vo = new VerificationResponse();
            vo.setStatus("none");
            return vo;
        }

        return convertToVO(verification);
    }

    private VerificationResponse convertToVO(Verification v) {
        VerificationResponse vo = new VerificationResponse();
        vo.setId(v.getId());
        vo.setRealName(v.getRealName());
        vo.setStudentId(v.getStudentId());
        vo.setCollege(v.getCollege());
        vo.setEnrollYear(v.getEnrollYear());
        vo.setStudentCardUrl(v.getStudentCardUrl());
        vo.setRejectReason(v.getRejectReason());
        vo.setCreatedAt(v.getCreatedAt());

        switch (v.getStatus()) {
            case 0 -> vo.setStatus("pending");
            case 1 -> vo.setStatus("approved");
            case 2 -> vo.setStatus("rejected");
            default -> vo.setStatus("none");
        }
        return vo;
    }
}
