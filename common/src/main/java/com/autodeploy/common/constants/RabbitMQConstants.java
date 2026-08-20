package com.autodeploy.common.constants;

/**
 * Centralized RabbitMQ exchange, queue, and routing-key constants.
 * All services must use these constants — never hardcode strings inline.
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    // ─── Exchanges (topic exchanges for flexible routing) ─────────────────────

    /** Exchange for all build-related events. */
    public static final String BUILD_EXCHANGE = "autodeploy.build.exchange";

    /** Exchange for all deployment-related events. */
    public static final String DEPLOYMENT_EXCHANGE = "autodeploy.deployment.exchange";

    /** Exchange for all notification-related events. */
    public static final String NOTIFICATION_EXCHANGE = "autodeploy.notification.exchange";

    // ─── Dead Letter Exchange ─────────────────────────────────────────────────

    public static final String DEAD_LETTER_EXCHANGE = "autodeploy.dlx";
    public static final String DEAD_LETTER_QUEUE = "autodeploy.dead-letter.queue";

    // ─── Queues ───────────────────────────────────────────────────────────────

    /** Consumed by build-service to start a new build. */
    public static final String BUILD_REQUESTED_QUEUE = "autodeploy.build.requested.queue";

    /** Consumed by deployment-service when a build succeeds. */
    public static final String BUILD_COMPLETED_QUEUE = "autodeploy.build.completed.queue";

    /** Consumed by notification-service when a build fails. */
    public static final String BUILD_FAILED_QUEUE = "autodeploy.build.failed.queue";

    /** Consumed by notification-service when a deployment succeeds. */
    public static final String DEPLOYMENT_COMPLETED_QUEUE = "autodeploy.deployment.completed.queue";

    /** Consumed by notification-service when a deployment fails. */
    public static final String DEPLOYMENT_FAILED_QUEUE = "autodeploy.deployment.failed.queue";

    // ─── Routing Keys ─────────────────────────────────────────────────────────

    public static final String BUILD_REQUESTED_RK = "build.requested";
    public static final String BUILD_COMPLETED_RK = "build.completed";
    public static final String BUILD_FAILED_RK = "build.failed";
    public static final String DEPLOYMENT_COMPLETED_RK = "deployment.completed";
    public static final String DEPLOYMENT_FAILED_RK = "deployment.failed";
}
