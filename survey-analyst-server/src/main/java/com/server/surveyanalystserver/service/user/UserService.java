package com.server.surveyanalystserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserLoginDTO;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param dto 注册信息
     * @return 用户信息
     */
    User register(UserRegisterDTO dto);

    /**
     * 用户登录
     * @param loginName 登录名（账号或邮箱）
     * @param password 密码
     * @return JWT Token
     */
    String login(String loginName, String password);

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    User getCurrentUser();

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUserInfo(User user);

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(String oldPassword, String newPassword);
}

