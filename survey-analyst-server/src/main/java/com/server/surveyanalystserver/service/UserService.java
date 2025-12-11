package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.User;
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

    /**
     * 发送密码重置邮件
     * @param email 邮箱
     */
    void sendPasswordResetEmail(String email);

    /**
     * 重置密码
     * @param token 重置令牌
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(String token, String newPassword);

    /**
     * 发送验证码
     * @param email 邮箱
     * @param type 类型（REGISTER-注册，RESET_PASSWORD-重置密码）
     * @return 是否成功
     */
    boolean sendVerificationCode(String email, String type);

    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型（REGISTER-注册，RESET_PASSWORD-重置密码）
     * @return 是否有效
     */
    boolean verifyCode(String email, String code, String type);

    /**
     * 使用验证码重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPasswordByCode(String email, String code, String newPassword);
}

