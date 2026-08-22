package com.tominnokoe.admin.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * IPv4アドレス・CIDR表記による簡易アクセス許可リスト判定。
 * LGWAN（総合行政ネットワーク）本番閉域網相当の「特定ネットワークからしかアクセスできない」
 * 制約を、実際の閉域網インフラを持たないこの環境で近似的に再現するための実装
 * （本番のLGWAN接続そのものではない点に注意。IPv6は簡易的に完全一致のみサポート）。
 */
public final class IpAllowlist {

    private IpAllowlist() {
    }

    public static boolean isAllowed(String remoteAddr, List<String> entries) {
        if (entries.isEmpty()) {
            return true; // 未設定時はアクセス制限を行わない（ローカルデモの利便性優先）
        }
        for (String entry : entries) {
            if (matches(remoteAddr, entry.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String remoteAddr, String entry) {
        if (entry.isEmpty()) {
            return false;
        }
        if (!entry.contains("/")) {
            return entry.equals(remoteAddr) || ("127.0.0.1".equals(remoteAddr) && "localhost".equals(entry));
        }
        String[] parts = entry.split("/", 2);
        try {
            int prefixLen = Integer.parseInt(parts[1]);
            byte[] network = InetAddress.getByName(parts[0]).getAddress();
            byte[] target = InetAddress.getByName(remoteAddr).getAddress();
            if (network.length != target.length) {
                return false; // IPv4/IPv6混在は非対応
            }
            return matchesPrefix(network, target, prefixLen);
        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }

    private static boolean matchesPrefix(byte[] network, byte[] target, int prefixLen) {
        int fullBytes = prefixLen / 8;
        int remainingBits = prefixLen % 8;
        for (int i = 0; i < fullBytes; i++) {
            if (network[i] != target[i]) {
                return false;
            }
        }
        if (remainingBits > 0 && fullBytes < network.length) {
            int mask = 0xFF << (8 - remainingBits) & 0xFF;
            if ((network[fullBytes] & mask) != (target[fullBytes] & mask)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> parseEnv(String csv) {
        List<String> result = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return result;
        }
        for (String part : csv.split(",")) {
            if (!part.isBlank()) {
                result.add(part.trim());
            }
        }
        return result;
    }
}
