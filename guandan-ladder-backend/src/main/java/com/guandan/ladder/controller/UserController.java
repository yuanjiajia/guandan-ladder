package com.guandan.ladder.controller;

import cn.hutool.core.collection.CollUtil;
import com.guandan.ladder.model.convert.UserConverter;
import com.guandan.ladder.model.entity.User;
import com.guandan.ladder.model.vo.UserVO;
import com.guandan.ladder.security.SecurityContext;
import com.guandan.ladder.service.UserService;
import com.hccake.ballcat.common.model.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hccake
 */
@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/list")
	public R<List<UserVO>> list() {
		List<User> users = userService.listAll();
		if (CollUtil.isEmpty(users)) {
			return R.ok(new ArrayList<>());
		}
		List<UserVO> vos = users.stream().map(UserConverter.INSTANCE::userToVo).collect(Collectors.toList());
		return R.ok(vos);
	}

	@GetMapping("/info")
	public R<UserVO> info() {
		String userId = SecurityContext.getUserId();
		User user = userService.queryByUid(userId);
		Assert.notNull(user, "用户不存在");

		UserVO userVO = UserConverter.INSTANCE.userToVo(user);
		return R.ok(userVO);
	}

}
