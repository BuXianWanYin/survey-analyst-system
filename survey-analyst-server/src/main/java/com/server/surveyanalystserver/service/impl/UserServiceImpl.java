package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.service.RedisCacheService;
import com.server.surveyanalystserver.service.UserService;
import com.server.surveyanalystserver.utils.DataEncryptionUtils;
import com.server.surveyanalystserver.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private com.server.surveyanalystserver.service.EmailService emailService;

    @Autowired
    private DataEncryptionUtils dataEncryptionUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Redis缓存过期时间（秒）- 15分钟
     */
    private static final long USER_CACHE_EXPIRE_TIME = 900L;

    /**
     * Redis缓存键前缀
     */
    private static final String USER_CACHE_KEY_PREFIX = "user:current:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(UserRegisterDTO dto) {
        // 检查账号是否已存在
        LambdaQueryWrapper<User> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(User::getAccount, dto.getAccount());
        if (this.count(accountWrapper) > 0) {
            throw new RuntimeException("账号已存在");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, dto.getEmail());
        if (this.count(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setAccount(dto.getAccount());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        
        // 如果注册时提供了手机号，进行加密（UserRegisterDTO中没有phone字段，这里预留）
        // 如果后续需要支持注册时填写手机号，可以在这里添加加密逻辑

        this.save(user);
        
        // 返回时解密手机号（如果存在）
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            try {
                user.setPhone(dataEncryptionUtils.decrypt(user.getPhone()));
            } catch (Exception e) {
                // 解密失败，可能是旧数据未加密，保持原值
            }
        }
        
        return user;
    }

    @Override
    public String login(String loginName, String password) {
        // 根据账号或邮箱查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getAccount, loginName).or().eq(User::getEmail, loginName));
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT Token（使用账号作为subject）
        return jwtUtils.generateToken(user.getAccount());
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }

        String username = authentication.getName();
        
        // 先从Redis缓存获取
        String cacheKey = USER_CACHE_KEY_PREFIX + username;
        User user = redisCacheService.get(cacheKey, User.class);
        
        if (user != null) {
            return user;
        }

        // 缓存未命中，从数据库查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, username);
        user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 解密手机号（如果存在）
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            try {
                user.setPhone(dataEncryptionUtils.decrypt(user.getPhone()));
            } catch (Exception e) {
                // 如果解密失败，可能是旧数据未加密，保持原值
            }
        }
        
        // 保存到Redis缓存（注意：缓存中存储的是解密后的数据）
        redisCacheService.set(cacheKey, user, USER_CACHE_EXPIRE_TIME);

        return user;
    }

    /**
     * 清除用户缓存
     * @param account 用户账号
     */
    private void clearUserCache(String account) {
        String cacheKey = USER_CACHE_KEY_PREFIX + account;
        redisCacheService.delete(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUserInfo(User user) {
        User currentUser = getCurrentUser();
        user.setId(currentUser.getId());
        
        // 如果更新了手机号，进行加密
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String encryptedPhone = dataEncryptionUtils.encrypt(user.getPhone());
            user.setPhone(encryptedPhone);
        }
        
        this.updateById(user);
        
        // 清除用户缓存
        clearUserCache(currentUser.getAccount());
        
        // 重新查询并返回（解密手机号）
        User updatedUser = this.getById(user.getId());
        if (updatedUser != null && updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
            try {
                updatedUser.setPhone(dataEncryptionUtils.decrypt(updatedUser.getPhone()));
            } catch (Exception e) {
                // 如果解密失败，可能是旧数据未加密，保持原值
            }
        }
        return updatedUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        boolean result = this.updateById(currentUser);
        
        // 清除用户缓存（密码已更新）
        if (result) {
            clearUserCache(currentUser.getAccount());
        }
        
        return result;
    }

    /**
     * 密码重置Token过期时间（秒）- 30分钟
     */
    private static final long PASSWORD_RESET_TOKEN_EXPIRE_TIME = 1800L;

    /**
     * 密码重置Token键前缀
     */
    private static final String PASSWORD_RESET_TOKEN_KEY_PREFIX = "password_reset:";

    /**
     * 验证码过期时间（秒）- 5分钟
     */
    private static final long VERIFICATION_CODE_EXPIRE_TIME = 300L;

    /**
     * 验证码发送间隔时间（秒）- 60秒
     */
    private static final long VERIFICATION_CODE_SEND_INTERVAL = 60L;

    /**
     * 验证码键前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";

    /**
     * 验证码发送时间键前缀
     */
    private static final String VERIFICATION_CODE_SEND_TIME_PREFIX = "verification_code_send_time:";

    @Override
    public void sendPasswordResetEmail(String email) {
        // 根据邮箱查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = this.getOne(wrapper);

        if (user == null) {
            // 为了安全，不提示用户是否存在，只记录日志
            return;
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成重置Token
        String token = java.util.UUID.randomUUID().toString();

        // 存储Token到Redis（30分钟过期）
        String tokenKey = PASSWORD_RESET_TOKEN_KEY_PREFIX + token;
        redisCacheService.set(tokenKey, user.getId(), PASSWORD_RESET_TOKEN_EXPIRE_TIME);

        // 发送密码重置邮件
        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String token, String newPassword) {
        // 从Redis获取Token对应的用户ID
        String tokenKey = PASSWORD_RESET_TOKEN_KEY_PREFIX + token;
        Long userId = redisCacheService.get(tokenKey, Long.class);

        if (userId == null) {
            throw new RuntimeException("重置令牌无效或已过期");
        }

        // 查询用户
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean result = this.updateById(user);

        // 删除Token（使用后立即删除）
        if (result) {
            redisCacheService.delete(tokenKey);
            // 清除用户缓存
            clearUserCache(user.getAccount());
        }

        return result;
    }

    @Override
    public boolean sendVerificationCode(String email, String type) {
        // 检查发送间隔（1分钟内只能发送一次）
        String sendTimeKey = VERIFICATION_CODE_SEND_TIME_PREFIX + type + ":" + email;
        Long lastSendTime = redisCacheService.get(sendTimeKey, Long.class);
        
        if (lastSendTime != null) {
            long currentTime = System.currentTimeMillis() / 1000;
            long elapsed = currentTime - lastSendTime;
            if (elapsed < VERIFICATION_CODE_SEND_INTERVAL) {
                long remaining = VERIFICATION_CODE_SEND_INTERVAL - elapsed;
                throw new RuntimeException("验证码发送过于频繁，请" + remaining + "秒后再试");
            }
        }

        // 如果是重置密码，检查邮箱是否已注册
        if ("RESET_PASSWORD".equals(type)) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, email);
            User user = this.getOne(wrapper);
            if (user == null) {
                // 为了安全，不提示用户是否存在
                throw new RuntimeException("如果该邮箱已注册，验证码已发送到您的邮箱");
            }
            if (user.getStatus() == 0) {
                throw new RuntimeException("用户已被禁用");
            }
        }

        // 生成6位数字验证码
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        // 存储验证码到Redis（5分钟过期）
        String codeKey = VERIFICATION_CODE_KEY_PREFIX + type + ":" + email;
        redisCacheService.set(codeKey, code, VERIFICATION_CODE_EXPIRE_TIME);

        // 记录发送时间（60秒过期）
        long currentTime = System.currentTimeMillis() / 1000;
        redisCacheService.set(sendTimeKey, currentTime, VERIFICATION_CODE_SEND_INTERVAL);

        // 发送验证码邮件
        try {
            emailService.sendVerificationCodeEmail(email, code, type);
            return true;
        } catch (Exception e) {
            // 发送失败，删除已存储的验证码
            redisCacheService.delete(codeKey);
            throw new RuntimeException("发送验证码失败：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String email, String code, String type) {
        String codeKey = VERIFICATION_CODE_KEY_PREFIX + type + ":" + email;
        String storedCode = redisCacheService.get(codeKey, String.class);
        
        if (storedCode == null) {
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        
        // 验证成功后删除验证码（防止重复使用）
        if (isValid) {
            redisCacheService.delete(codeKey);
        }
        
        return isValid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPasswordByCode(String email, String code, String newPassword) {
        // 验证验证码
        if (!verifyCode(email, code, "RESET_PASSWORD")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean result = this.updateById(user);

        // 删除验证码（使用后立即删除）
        if (result) {
            String codeKey = VERIFICATION_CODE_KEY_PREFIX + "RESET_PASSWORD:" + email;
            redisCacheService.delete(codeKey);
            // 清除用户缓存
            clearUserCache(user.getAccount());
        }

        return result;
    }
}

