package com.campus360.platform.tenancy;

/**
 * Holds the current request's tenant (institution) id in a ThreadLocal.
 * Populated by {@link com.campus360.platform.security.JwtAuthFilter} from the
 * authenticated user's JWT claim, and read by services to scope every query to
 * the caller's institution. SUPER_ADMIN requests may carry a null tenant.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    /** Tenant id or throw — use in code paths that must be tenant-scoped. */
    public static Long requireTenantId() {
        Long id = CURRENT_TENANT.get();
        if (id == null) {
            throw new IllegalStateException("No tenant in context for this request");
        }
        return id;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
