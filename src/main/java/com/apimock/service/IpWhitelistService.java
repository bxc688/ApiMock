package com.apimock.service;

import com.apimock.util.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IP 白名单服务：开关和名单均读配置文件（application.yml）
 * 配置项：
 *   apimock.ip-whitelist.enabled  - 是否启用白名单校验
 *   apimock.ip-whitelist.list     - 白名单 IP 列表（逗号分隔，支持单 IP 和 CIDR 网段）
 */
@Service
public class IpWhitelistService {

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistService.class);

    @Value("${apimock.ip-whitelist.enabled:false}")
    private boolean enabled;

    @Value("${apimock.ip-whitelist.list:}")
    private String listRaw;

    /** 启用的白名单表达式缓存 */
    private volatile List<String> enabledExprs = Collections.emptyList();

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 重新加载缓存（配置变更后需重启服务） */
    public void refresh() {
        List<String> exprs = new ArrayList<>();
        if (listRaw != null && !listRaw.trim().isEmpty()) {
            for (String raw : listRaw.split(",")) {
                String expr = raw.trim();
                if (!expr.isEmpty() && IpUtils.isValidExpr(expr)) {
                    exprs.add(expr);
                } else if (!expr.isEmpty()) {
                    log.warn("忽略无效的 IP 白名单表达式: {}", expr);
                }
            }
        }
        this.enabledExprs = Collections.unmodifiableList(exprs);
        log.info("IP 白名单已加载 {} 条（开关：{}）", exprs.size(), enabled ? "启用" : "关闭");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** 判断客户端 IP 是否允许访问 */
    public boolean isAllowed(String clientIp) {
        for (String expr : enabledExprs) {
            if (IpUtils.matches(clientIp, expr)) {
                return true;
            }
        }
        return false;
    }
}
