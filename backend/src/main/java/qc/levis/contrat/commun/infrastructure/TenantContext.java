package qc.levis.contrat.commun.infrastructure;

/**
 * Contexte de tenant courant (par requête), porté par un ThreadLocal.
 * Alimenté par le filtre JWT depuis le claim « tenant ». {@code null} = superadmin
 * (global, hors tenant).
 */
public final class TenantContext {
    private TenantContext() {}

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    public static void set(Long tenantId) { CURRENT.set(tenantId); }
    public static Long get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
    public static boolean estSuperadmin() { return CURRENT.get() == null; }
}
