package com.autodeploy.common.constants;

/**
 * Centralized Redis key and pub/sub channel name patterns.
 * All services must use these constants — never hardcode strings inline.
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    // ─── Pub/Sub Channels ─────────────────────────────────────────────────────

    /** Channel pattern for real-time build log lines: {@code autodeploy:build-logs:{buildId}} */
    private static final String BUILD_LOGS_CHANNEL = "autodeploy:build-logs:%s";

    /** Channel pattern for real-time deployment log lines. */
    private static final String DEPLOY_LOGS_CHANNEL = "autodeploy:deploy-logs:%s";

    /** Channel pattern for build status updates. */
    private static final String BUILD_STATUS_CHANNEL = "autodeploy:build-status:%s";

    // ─── Cache / State Keys ───────────────────────────────────────────────────

    /** Rate limit counter key per user: {@code autodeploy:rate-limit:{userId}} */
    private static final String RATE_LIMIT_KEY = "autodeploy:rate-limit:%s";

    /** User session data: {@code autodeploy:session:{userId}} */
    private static final String USER_SESSION_KEY = "autodeploy:session:%s";

    /** Active build count per user: {@code autodeploy:active-builds:{userId}} */
    private static final String ACTIVE_BUILDS_KEY = "autodeploy:active-builds:%s";

    // ─── Factory Methods ──────────────────────────────────────────────────────

    public static String buildLogsChannel(String buildId) {
        return String.format(BUILD_LOGS_CHANNEL, buildId);
    }

    public static String deployLogsChannel(String deploymentId) {
        return String.format(DEPLOY_LOGS_CHANNEL, deploymentId);
    }

    public static String buildStatusChannel(String buildId) {
        return String.format(BUILD_STATUS_CHANNEL, buildId);
    }

    public static String rateLimitKey(String userId) {
        return String.format(RATE_LIMIT_KEY, userId);
    }

    public static String userSessionKey(String userId) {
        return String.format(USER_SESSION_KEY, userId);
    }

    public static String activeBuildsKey(String userId) {
        return String.format(ACTIVE_BUILDS_KEY, userId);
    }
}
