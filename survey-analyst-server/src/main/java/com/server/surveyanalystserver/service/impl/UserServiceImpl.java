package com.server.surveyanalystserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.entity.dto.UserRegisterDTO;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.service.EmailService;
import com.server.surveyanalystserver.service.OperationLogService;
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

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private EmailService emailService;

    @Autowired
    private DataEncryptionUtils dataEncryptionUtils;

    @Autowired
    private OperationLogService operationLogService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Redis缓存过期时间（秒）- 15分钟
     */
    private static final long USER_CACHE_EXPIRE_TIME = 900L;

    /**
     * Redis缓存键前缀
     */
    private static final String USER_CACHE_KEY_PREFIX = "user:current:";

    /**
     * 用户注册
     * 创建新用户账号，验证账号和邮箱唯一性，密码使用BCrypt加密存储
     * @param dto 注册信息DTO，包含账号、邮箱、用户名、密码等
     * @return 注册成功后的用户信息
     * @throws RuntimeException 如果账号或邮箱已存在则抛出异常
     */
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

    /**
     * 用户登录
     * 验证用户账号和密码，返回JWT Token用于后续请求认证
     * @param loginName 登录名，可以是账号或邮箱
     * @param password 用户密码（明文）
     * @return JWT Token字符串
     * @throws RuntimeException 如果用户不存在、已被禁用或密码错误则抛出异常
     */
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

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中获取当前认证用户，优先从Redis缓存获取，缓存未命中则查询数据库
     * @return 当前用户信息
     * @throws RuntimeException 如果未登录或用户不存在则抛出异常
     */
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

    /**
     * 更新用户信息
     * 更新当前登录用户的基本信息，如果更新了手机号则进行加密存储，并清除用户缓存
     * @param user 包含更新信息的用户对象，必须包含id
     * @return 更新后的用户信息（手机号已解密）
     */
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

    /**
     * 修改密码
     * 验证旧密码后更新为新密码，并清除用户缓存
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return true表示修改成功，false表示修改失败
     * @throws RuntimeException 如果旧密码错误则抛出异常
     */
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

    /**
     * 发送密码重置邮件
     * 生成重置Token并发送到用户邮箱，Token存储在Redis中，30分钟过期
     * @param email 用户邮箱
     * @throws RuntimeException 如果用户已被禁用则抛出异常
     */
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
        String token = UUID.randomUUID().toString();

        // 存储Token到Redis（30分钟过期）
        String tokenKey = PASSWORD_RESET_TOKEN_KEY_PREFIX + token;
        redisCacheService.set(tokenKey, user.getId(), PASSWORD_RESET_TOKEN_EXPIRE_TIME);

        // 发送密码重置邮件
        emailService.sendPasswordResetEmail(email, token);
    }

    /**
     * 重置密码
     * 根据重置Token验证用户身份后更新密码，Token使用后立即删除
     * @param token 密码重置Token
     * @param newPassword 新密码（明文）
     * @return true表示重置成功，false表示重置失败
     * @throws RuntimeException 如果Token无效或已过期、用户不存在则抛出异常
     */
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

    /**
     * 发送验证码
     * 生成6位数字验证码并发送到用户邮箱，验证码存储在Redis中，5分钟过期
     * 限制发送频率：1分钟内只能发送一次
     * @param email 用户邮箱
     * @param type 验证码类型（RESET_PASSWORD-重置密码，REGISTER-注册）
     * @return true表示发送成功，false表示发送失败
     * @throws RuntimeException 如果发送过于频繁、邮箱未注册（重置密码场景）或邮箱已注册（注册场景）则抛出异常
     */
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

    /**
     * 验证验证码
     * 验证用户输入的验证码是否正确，验证成功后删除验证码（防止重复使用）
     * @param email 用户邮箱
     * @param code 用户输入的验证码
     * @param type 验证码类型（RESET_PASSWORD-重置密码，REGISTER-注册）
     * @return true表示验证码正确，false表示验证码错误或已过期
     */
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

    /**
     * 通过验证码重置密码
     * 验证验证码后更新用户密码，验证码使用后立即删除
     * @param email 用户邮箱
     * @param code 验证码
     * @param newPassword 新密码（明文）
     * @return true表示重置成功，false表示重置失败
     * @throws RuntimeException 如果验证码错误或已过期、用户不存在、用户已被禁用则抛出异常
     */
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

    /**
     * 通过Map参数注册用户
     * 从Map中提取注册信息并验证验证码后调用register方法
     * @param params 注册参数Map，包含account、email、username、password、code
     * @return 注册成功后的用户信息
     * @throws RuntimeException 如果参数为空、验证码错误或已过期则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User registerWithMap(Map<String, String> params) {
        String account = params.get("account");
        String email = params.get("email");
        String username = params.get("username");
        String password = params.get("password");
        String code = params.get("code");

        if (account == null || account.trim().isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }

        boolean valid = verifyCode(email.trim(), code.trim(), "REGISTER");
        if (!valid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setAccount(account.trim());
        dto.setEmail(email.trim());
        dto.setUsername(username.trim());
        dto.setPassword(password.trim());

        return register(dto);
    }

    /**
     * 用户登录（返回完整结果）
     * 执行登录并返回Token和用户信息，同时记录登录操作日志
     * @param loginName 登录名，可以是账号或邮箱
     * @param password 用户密码（明文）
     * @param request HTTP请求对象，用于获取IP地址
     * @return 包含token和user的Map
     * @throws RuntimeException 如果登录失败则抛出异常
     */
    @Override
    public Map<String, Object> loginWithResult(String loginName, String password, HttpServletRequest request) {
        String token = login(loginName, password);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(User::getAccount, loginName).or().eq(User::getEmail, loginName));
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(null);
        user.setPhone(null);

        try {
            OperationLog log = new OperationLog();
            log.setUserId(user.getId());
            log.setOperationType("登录");
            log.setOperationDesc("用户登录：" + user.getUsername());
            log.setRequestUrl("/api/auth/login");
            log.setRequestMethod("POST");
            log.setIpAddress(getIpAddress(request));
            log.setCreateTime(LocalDateTime.now());
            operationLogService.saveLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响登录
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return data;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 用户登出
     * 记录登出操作日志
     * @param request HTTP请求对象，用于获取IP地址
     */
    @Override
    public void logout(HttpServletRequest request) {
        User currentUser = getCurrentUser();
        try {
            OperationLog log = new OperationLog();
            log.setUserId(currentUser.getId());
            log.setOperationType("登出");
            log.setOperationDesc("用户登出：" + currentUser.getUsername());
            log.setRequestUrl("/api/user/logout");
            log.setRequestMethod("POST");
            log.setIpAddress(getIpAddress(request));
            log.setCreateTime(LocalDateTime.now());
            operationLogService.saveLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响登出
        }
    }

    /**
     * 管理员分页查询用户列表
     * 支持按关键词（账号、邮箱、用户名）和状态筛选用户
     * @param page 分页参数
     * @param keyword 关键词，用于搜索账号、邮箱、用户名，可选
     * @param status 用户状态（0-禁用，1-启用），可选
     * @return 用户分页列表
     */
    @Override
    public Page<User> getAdminUserList(Page<User> page, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getAccount, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getUsername, keyword));
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 管理员更新用户信息
     * 更新指定用户的信息
     * @param id 用户ID
     * @param user 包含更新信息的用户对象
     * @return 更新后的用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateAdminUser(Long id, User user) {
        user.setId(id);
        this.updateById(user);
        return this.getById(id);
    }

    /**
     * 更新用户状态
     * 启用或禁用指定用户
     * @param id 用户ID
     * @param status 用户状态（0-禁用，1-启用）
     * @throws RuntimeException 如果用户不存在则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        User user = this.getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        this.updateById(user);
    }

    /**
     * 管理员创建用户
     * 创建新用户，验证账号和邮箱唯一性，密码使用BCrypt加密存储
     * @param user 用户信息对象，必须包含account、email、username、password
     * @return 创建成功后的用户信息
     * @throws RuntimeException 如果账号或邮箱已存在则抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createAdminUser(User user) {
        LambdaQueryWrapper<User> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(User::getAccount, user.getAccount());
        if (this.count(accountWrapper) > 0) {
            throw new RuntimeException("账号已存在");
        }

        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, user.getEmail());
        if (this.count(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        this.save(user);
        return this.getById(user.getId());
    }

    /**
     * 获取用户统计信息
     * 统计系统中的用户总数、启用用户数、禁用用户数
     * @return 统计数据Map，包含totalUsers、activeUsers、disabledUsers字段
     */
    @Override
    public Map<String, Object> getUserStatistics() {
        long totalUsers = this.count();
        long activeUsers = this.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long disabledUsers = this.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("disabledUsers", disabledUsers);
        
        return statistics;
    }
}

