package com.apimock.service;

import com.apimock.mapper.UserMapper;
import com.apimock.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Token 有效期：30 分钟（毫秒） */
    private static final long TOKEN_EXPIRE_MS = 30 * 60 * 1000L;

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    /** Token -> 过期时间戳 */
    private final ConcurrentHashMap<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 登录验证
     * @return token，失败返回 null
     */
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", username);
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败：密码错误 - {}", username);
            return null;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, System.currentTimeMillis() + TOKEN_EXPIRE_MS);
        log.info("用户登录成功：{}", username);
        return token;
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        Long expireTime = tokenStore.get(token);
        if (expireTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > expireTime) {
            tokenStore.remove(token);
            return false;
        }
        return true;
    }

    /**
     * 登出（清除 Token）
     */
    public void logout(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }

    /**
     * 清理过期 Token（可定时调用）
     */
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(entry -> now > entry.getValue());
    }
}
