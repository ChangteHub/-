package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.Verification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VerificationRepository extends BaseMapper<Verification> {
}
