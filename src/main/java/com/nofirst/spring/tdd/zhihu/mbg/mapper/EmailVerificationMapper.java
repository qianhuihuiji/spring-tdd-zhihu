package com.nofirst.spring.tdd.zhihu.mbg.mapper;

import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerification;
import com.nofirst.spring.tdd.zhihu.mbg.model.EmailVerificationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EmailVerificationMapper {
    long countByExample(EmailVerificationExample example);

    int deleteByExample(EmailVerificationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(EmailVerification row);

    int insertSelective(EmailVerification row);

    List<EmailVerification> selectByExample(EmailVerificationExample example);

    EmailVerification selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("row") EmailVerification row, @Param("example") EmailVerificationExample example);

    int updateByExample(@Param("row") EmailVerification row, @Param("example") EmailVerificationExample example);

    int updateByPrimaryKeySelective(EmailVerification row);

    int updateByPrimaryKey(EmailVerification row);
}