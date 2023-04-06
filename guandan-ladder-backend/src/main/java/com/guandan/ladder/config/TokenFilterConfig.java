package com.guandan.ladder.config;

import cn.hutool.core.codec.Base64;
import com.hccake.ballcat.common.model.result.R;
import com.hccake.ballcat.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yuanjiajia 2023/4/3
 */
@Configuration
public class TokenFilterConfig {

	private static final String TOKEN = "token";

	private static final String LOGIN_PATH = "/login";

	private static final String NONE_PATH = "/";

	@Resource
	private TokenMap tokenMap = new TokenMap();

	@Bean
	public FilterRegistrationBean<OncePerRequestFilter> tokenCheckFiler() {
		FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(createCheckTokenFilter());
		registration.addUrlPatterns("/*");
		registration.setName("tokenCheckFiler");
		registration.setOrder(0);
		return registration;
	}

	private OncePerRequestFilter createCheckTokenFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
					throws ServletException, IOException {
				String requestUri = req.getServletPath();
				if (!LOGIN_PATH.equals(requestUri) && !NONE_PATH.equals(requestUri)) {
					// 非登录接口需要判断token
					if (!checkToken(req.getHeader(TOKEN))) {
						res.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
						R<Void> failedResult = R.failed(1000, "未获取到登录信息，请登录");
						res.getWriter().write(JsonUtils.toJson(failedResult));
						return;
					}
				}
				try {
					// 将用户id存到本地线程 todo 处理成用户id
					UserContext.setUserId(req.getHeader(TOKEN));
					chain.doFilter(req, res);
				}
				finally {
					// 清除本地线程 避免线程复用带来错误
					UserContext.clear();
				}
			}
		};
	}

	private boolean checkToken(String token) {
		if (StringUtils.isNoneBlank(token)) {
			// 有token则需要校验是否合法 token：【userName-UUID】
			String decodeStr = Base64.decodeStr(token);
			String[] split = decodeStr.split("-");
			String clientToken = tokenMap.getToken(split[0]);
			return StringUtils.isNoneBlank(clientToken) && clientToken.equals(split[1]);
		}
		return false;
	}

}