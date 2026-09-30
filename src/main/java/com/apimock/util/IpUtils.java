package com.apimock.util;

/**
 * IP 工具：支持单 IP 与 CIDR 网段匹配（IPv4）
 */
public final class IpUtils {

    private IpUtils() {
    }

    /**
     * IPv4 转无符号 long（0 ~ 2^32-1），非法返回 -1
     */
    public static long toLong(String ip) {
        if (ip == null) {
            return -1;
        }
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) {
            return -1;
        }
        long value = 0;
        for (String part : parts) {
            if (part.isEmpty()) {
                return -1;
            }
            int n;
            try {
                n = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return -1;
            }
            if (n < 0 || n > 255) {
                return -1;
            }
            value = (value << 8) | n;
        }
        return value;
    }

    /**
     * 判断客户端 IP 是否匹配白名单表达式
     * 支持：
     *   192.168.1.100     单 IP
     *   192.168.1.0/24    CIDR 网段
     * @param clientIp 客户端 IP
     * @param expr     白名单表达式
     */
    public static boolean matches(String clientIp, String expr) {
        long ip = toLong(clientIp);
        if (ip < 0) {
            return false;
        }
        if (expr == null || expr.trim().isEmpty()) {
            return false;
        }
        String e = expr.trim();

        // CIDR：192.168.1.0/24
        int slash = e.indexOf('/');
        if (slash > 0) {
            long base = toLong(e.substring(0, slash));
            if (base < 0) {
                return false;
            }
            int bits;
            try {
                bits = Integer.parseInt(e.substring(slash + 1).trim());
            } catch (NumberFormatException ex) {
                return false;
            }
            if (bits < 0 || bits > 32) {
                return false;
            }
            long mask = (bits == 0) ? 0L : (0xFFFFFFFFL << (32 - bits)) & 0xFFFFFFFFL;
            return (ip & mask) == (base & mask);
        }

        // 单 IP
        long single = toLong(e);
        return single >= 0 && single == ip;
    }

    /**
     * 校验表达式是否合法（用于新增时提示）
     */
    public static boolean isValidExpr(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return false;
        }
        String e = expr.trim();
        int slash = e.indexOf('/');
        if (slash > 0) {
            if (toLong(e.substring(0, slash)) < 0) {
                return false;
            }
            try {
                int bits = Integer.parseInt(e.substring(slash + 1).trim());
                return bits >= 0 && bits <= 32;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return toLong(e) >= 0;
    }

    /**
     * 是否本机回环地址（硬编码放行，防止把自己锁在外面）
     */
    public static boolean isLoopback(String ip) {
        if (ip == null) {
            return false;
        }
        String s = ip.trim();
        return s.startsWith("127.") || "::1".equals(s) || "0:0:0:0:0:0:0:1".equals(s)
                || "localhost".equalsIgnoreCase(s);
    }
}
