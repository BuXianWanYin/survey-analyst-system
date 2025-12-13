package com.server.surveyanalystserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 创建新用户账号，密码使用BCrypt加密存储
     * @param dto 注册信息DTO，包含账号、邮箱、用户名、密码等
     * @return 注册成功后的用户信息
     */
    User register(UserRegisterDTO dto);

    /**
     * 用户登录
     * 验证用户账号和密码，返回JWT Token用于后续请求认证
     * @param loginName 登录名，可以是账号或邮箱
     * @param password 用户密码（明文）
     * @return JWT Token字符串
     */
    String login(String loginName, String password);

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中获取当前认证用户的信息
     * @return 当前用户信息，如果未登录则返回null
     */
    User getCurrentUser();

    /**
     * 更新当前用户信息
     * 更新当前登录用户的个人信息（不包括密码）
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    User updateUserInfo(User user);

    /**
     * 修改当前用户密码
     * 验证旧密码后更新为新密码，新密码使用BCrypt加密存储
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return true表示修改成功，false表示旧密码验证失败
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

    /**
     * 用户注册（使用Map参数）
     * 从Map中提取注册信息并创建新用户，支持验证码验证
     * @param params 注册参数Map，包含account、email、username、password、code等字段
     * @return 注册成功后的用户信息
     */
    User registerWithMap(Map<String, String> params);

    /**
     * 用户登录并返回完整登录结果
     * 验证用户身份后返回JWT Token和用户信息，同时记录登录日志
     * @param loginName 登录名，可以是账号或邮箱
     * @param password 用户密码（明文）
     * @param request HTTP请求对象，用于获取IP地址等信息
     * @return 登录结果Map，包含token和user信息
     */
    Map<String, Object> loginWithResult(String loginName, String password, HttpServletRequest request);

    /**
     * 用户登出
     * @param request HTTP请求对象
     */
    void logout(HttpServletRequest request);

    /**
     * 管理员分页查询用户列表
     * @param page 分页参数
     * @param keyword 关键词（可选）
     * @param status 状态（可选）
     * @return 用户分页列表
     */
    Page<User> getAdminUserList(Page<User> page, String keyword, Integer status);

    /**
     * 管理员更新用户信息
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateAdminUser(Long id, User user);

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 管理员创建用户
     * @param user 用户信息
     * @return 创建的用户信息
     */
    User createAdminUser(User user);

    /**
     * 获取用户统计数据
     * @return 统计数据
     */
    Map<String, Object> getUserStatistics();
}

