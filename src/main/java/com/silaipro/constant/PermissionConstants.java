package com.silaipro.constant;

/**
 * Central registry of all permission string constants used in @HasPermission annotations.
 * These must match the values stored in the roles.permissions_json field.
 */
public final class PermissionConstants {

    private PermissionConstants() {}

    // ── Customer ──────────────────────────────────────────────────────────────
    public static final String CUSTOMER_CREATE = "CUSTOMER_CREATE";
    public static final String CUSTOMER_EDIT   = "CUSTOMER_EDIT";
    public static final String CUSTOMER_DELETE = "CUSTOMER_DELETE";
    public static final String CUSTOMER_VIEW   = "CUSTOMER_VIEW";

    // ── Measurements ──────────────────────────────────────────────────────────
    public static final String MEASUREMENT_ADD    = "MEASUREMENT_ADD";
    public static final String MEASUREMENT_VIEW   = "MEASUREMENT_VIEW";
    public static final String MEASUREMENT_PRINT  = "MEASUREMENT_PRINT";
    public static final String MEASUREMENT_CONFIG = "MEASUREMENT_CONFIG";

    // ── Orders ────────────────────────────────────────────────────────────────
    public static final String ORDER_CREATE = "ORDER_CREATE";
    public static final String ORDER_EDIT   = "ORDER_EDIT";
    public static final String ORDER_DELETE = "ORDER_DELETE";
    public static final String ORDER_VIEW   = "ORDER_VIEW";

    // ── Billing & Payments ────────────────────────────────────────────────────
    public static final String BILLING_CREATE  = "BILLING_CREATE";
    public static final String PAYMENT_RECORD  = "PAYMENT_RECORD";

    // ── Accounts & Reports ────────────────────────────────────────────────────
    public static final String ACCOUNTS_VIEW   = "ACCOUNTS_VIEW";
    public static final String ACCOUNTS_REPORT = "ACCOUNTS_REPORT";

    // ── Messaging ─────────────────────────────────────────────────────────────
    public static final String MESSAGE_SEND = "MESSAGE_SEND";

    // ── Staff Management ──────────────────────────────────────────────────────
    public static final String STAFF_MANAGE = "STAFF_MANAGE";

    // ── Settings ──────────────────────────────────────────────────────────────
    public static final String SETTINGS_MANAGE = "SETTINGS_MANAGE";

    // ── Convenience: permission sets per default role ─────────────────────────
    /** All permissions — used by Admin role. */
    public static final String[] ALL_PERMISSIONS = {
        CUSTOMER_CREATE, CUSTOMER_EDIT, CUSTOMER_DELETE, CUSTOMER_VIEW,
        MEASUREMENT_ADD, MEASUREMENT_VIEW, MEASUREMENT_PRINT, MEASUREMENT_CONFIG,
        ORDER_CREATE, ORDER_EDIT, ORDER_DELETE, ORDER_VIEW,
        BILLING_CREATE, PAYMENT_RECORD,
        ACCOUNTS_VIEW, ACCOUNTS_REPORT,
        MESSAGE_SEND,
        STAFF_MANAGE,
        SETTINGS_MANAGE
    };

    /** Manager: all except STAFF_MANAGE, SETTINGS_MANAGE. */
    public static final String[] MANAGER_PERMISSIONS = {
        CUSTOMER_CREATE, CUSTOMER_EDIT, CUSTOMER_DELETE, CUSTOMER_VIEW,
        MEASUREMENT_ADD, MEASUREMENT_VIEW, MEASUREMENT_PRINT, MEASUREMENT_CONFIG,
        ORDER_CREATE, ORDER_EDIT, ORDER_DELETE, ORDER_VIEW,
        BILLING_CREATE, PAYMENT_RECORD,
        ACCOUNTS_VIEW, ACCOUNTS_REPORT,
        MESSAGE_SEND
    };

    /** Staff: operational day-to-day access. */
    public static final String[] STAFF_PERMISSIONS = {
        CUSTOMER_CREATE, CUSTOMER_EDIT, CUSTOMER_VIEW,
        MEASUREMENT_ADD, MEASUREMENT_VIEW,
        ORDER_CREATE, ORDER_EDIT, ORDER_VIEW,
        BILLING_CREATE, PAYMENT_RECORD,
        MESSAGE_SEND
    };

    /** View Only: read access + print. */
    public static final String[] VIEW_ONLY_PERMISSIONS = {
        CUSTOMER_VIEW,
        MEASUREMENT_VIEW, MEASUREMENT_PRINT,
        ORDER_VIEW
    };
}
