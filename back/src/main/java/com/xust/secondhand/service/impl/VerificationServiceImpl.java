package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.dto.VerificationDTO;
import com.xust.secondhand.entity.Verification;
import com.xust.secondhand.mapper.VerificationMapper;
import com.xust.secondhand.service.VerificationService;
import com.xust.secondhand.vo.VerificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationMapper verificationMapper;

    @Override
    public void submit(Long userId, VerificationDTO dto) {
        // 检查是否已有待审核或已通过的记录
        LambdaQueryWrapper<Verification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Verification::getUserId, userId)
               .in(Verification::getStatus, 0, 1);
        if (verificationMapper.selectCount(wrapper) > 0) {
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
        verificationMapper.insert(verification);
    }

    @Override
    public VerificationVO getStatus(Long userId) {
        LambdaQueryWrapper<Verification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Verification::getUserId, userId)
               .orderByDesc(Verification::getCreatedAt)
               .last("LIMIT 1");
        Verification verification = verificationMapper.selectOne(wrapper);

        if (verification == null) {
            VerificationVO vo = new VerificationVO();
            vo.setStatus("none");
            return vo;
        }

        return convertToVO(verification);
    }

    private VerificationVO convertToVO(Verification v) {
        VerificationVO vo = new VerificationVO();
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
