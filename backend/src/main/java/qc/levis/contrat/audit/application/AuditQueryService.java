package qc.levis.contrat.audit.application;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/**
 * Lecture et export du journal d'audit (art. 18) : consultation filtrée et export CSV,
 * <em>sans coupler le chemin d'écriture</em> (ADR-006). Toujours scopé au tenant courant.
 */
@Service
public class AuditQueryService {

    private final AuditLogRepository repo;

    public AuditQueryService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /** Événements du tenant courant filtrés (tous critères optionnels), plus récents d'abord. */
    public List<AuditLog> rechercher(String utilisateur, String action, String entite,
                                     Instant depuis, Instant jusqua) {
        return repo.rechercher(TenantContext.get(),
                vide(utilisateur), vide(action), vide(entite), depuis, jusqua);
    }

    /** Export CSV (RFC 4180) des événements filtrés — format ouvert et exploitable. */
    public String exporterCsv(String utilisateur, String action, String entite,
                              Instant depuis, Instant jusqua) {
        StringBuilder csv = new StringBuilder("horodatage,utilisateur,action,entite,entiteId,details\n");
        for (AuditLog a : rechercher(utilisateur, action, entite, depuis, jusqua)) {
            csv.append(champ(a.getHorodatage())).append(',')
               .append(champ(a.getUtilisateur())).append(',')
               .append(champ(a.getAction())).append(',')
               .append(champ(a.getEntite())).append(',')
               .append(champ(a.getEntiteId())).append(',')
               .append(champ(a.getDetails())).append('\n');
        }
        return csv.toString();
    }

    private static String vide(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Échappe une valeur CSV : guillemets doublés et encadrement si caractère spécial. */
    private static String champ(Object valeur) {
        if (valeur == null) return "";
        String s = valeur.toString();
        if (s.contains("\"") || s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
