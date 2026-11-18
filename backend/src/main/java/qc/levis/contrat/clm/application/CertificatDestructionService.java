package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.port.CachetElectronique;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Certificat de destruction (addenda 3, Q48) : le bordereau consigné à la destruction, rendu en PDF et scellé par le
 * cachet électronique (PAdES). Il prouve, sans la plateforme, quels éléments ont été détruits, quand, par quelle autorité,
 * par quelle méthode et avec quel résultat.
 */
@Service
public class CertificatDestructionService {

    private final TrousseRepository trousses;
    private final RenduDocuments rendu;
    private final CachetElectronique cachet;
    private final ContexteClm ctx;

    public CertificatDestructionService(TrousseRepository trousses, RenduDocuments rendu, CachetElectronique cachet, ContexteClm ctx) {
        this.trousses = trousses;
        this.rendu = rendu;
        this.cachet = cachet;
        this.ctx = ctx;
    }

    public byte[] certificat(Long trousseId) {
        Trousse t = trousses.findByIdAndTenantId(trousseId, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Trousse introuvable."));
        if (t.getBordereauDestruction() == null) {
            throw new IllegalStateException("Aucun bordereau : la trousse n'a pas été détruite.");
        }
        Map<String, Object> b = ctx.lireObjet(t.getBordereauDestruction());
        String pdf = texte(b);
        return cachet.scellerPdf(rendu.genererPdf("Certificat de destruction — " + b.get("reference"), pdf),
                "Certificat de destruction " + b.get("reference"));
    }

    static String texte(Map<String, Object> b) {
        StringBuilder sb = new StringBuilder();
        sb.append("Référence : ").append(b.get("reference")).append('\n')
                .append("Titre : ").append(b.get("titre")).append('\n')
                .append("Type de contrat : ").append(b.get("typeContrat") == null ? "—" : b.get("typeContrat")).append('\n')
                .append("Règle de conservation : ").append(b.get("regle")).append(" (échéance ").append(b.get("echeance")).append(")\n")
                .append("Date de destruction : ").append(b.get("date")).append('\n');
        Map<?, ?> autorite = (Map<?, ?>) b.get("autorite");
        sb.append("Autorité : ").append(autorite.get("acteur")).append(" — ").append(autorite.get("role")).append('\n')
                .append("Méthode : ").append(b.get("methode")).append('\n')
                .append("Résultat : ").append(b.get("resultat")).append("\n\nÉléments détruits :\n");
        for (Object o : (List<?>) b.get("elements")) {
            Map<?, ?> e = (Map<?, ?>) o;
            sb.append("  • ").append(e.get("nom")).append(" v").append(e.get("version")).append(" — SHA-256 ").append(e.get("sha256")).append('\n');
        }
        Map<?, ?> anonymises = (Map<?, ?>) b.get("anonymises");
        sb.append("\nDonnées anonymisées : ").append(anonymises.get("participants")).append(" participant(s), ")
                .append(anonymises.get("invitations")).append(" invitation(s), ").append(anonymises.get("decisions"))
                .append(" décision(s), ").append(anonymises.get("analyses")).append(" analyse(s) supprimée(s).\n");
        return sb.toString();
    }
}
