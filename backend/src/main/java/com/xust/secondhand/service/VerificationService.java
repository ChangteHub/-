package com.xust.secondhand.service;

import com.xust.secondhand.dto.request.VerificationRequest;
import com.xust.secondhand.dto.response.VerificationResponse;

public interface VerificationService {

    void submit(Long userId, VerificationRequest dto);

    VerificationResponse getStatus(Long userId);
}
