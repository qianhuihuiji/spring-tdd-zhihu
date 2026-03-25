package com.nofirst.spring.tdd.zhihu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The type Jwt authentication entry point.
 */
@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = "请先登录";
        // 检查原始异常或其原因是否是 EmailNotVerifiedException
        if (e instanceof EmailNotVerifiedException || (e.getCause() != null && e.getCause() instanceof EmailNotVerifiedException)) {
            message = "请先验证邮箱";
        }

        CommonResult<String> resultDTO = CommonResult.error(message);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.write(objectMapper.writeValueAsString(resultDTO).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
