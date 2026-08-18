package com.xust.secondhand.service;

import com.xust.secondhand.dto.VerificationDTO;
import com.xust.secondhand.vo.VerificationVO;

public interface VerificationService {

    void submit(Long userId, VerificationDTO dto);

    VerificationVO getStatus(Long userId);
}
