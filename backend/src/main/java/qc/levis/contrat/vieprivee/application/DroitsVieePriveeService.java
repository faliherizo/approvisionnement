package qc.levis.contrat.vieprivee.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.application.port.TirageHasard;
import qc.levis.contrat.vieprivee.application.repository.DemandeVieePriveeRepository;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.DemandeVieePrivee;
import qc.levis.contrat.vieprivee.domain.DossierRenseignements;
import qc.levis.contrat.vieprivee.domain.EffacementRefuse;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;

/**
 * Droits de la personne concernée (Loi 25, art. 27 à 28.1 ; Lévis ENF1, C-15) : accès à ses renseignements dans un format
 * structuré, puis effacement. Chaque module détenteur répond par son port ; chaque demande est inscrite au registre.
 */
@Service
public class DroitsVieePriveeService {

    private final List<SourceRenseignementsPersonnels> sources;
    private final DemandeVieePriveeRepository registre;
    private final AuditService audit;
    private final TirageHasard hasard;
    private final Clock horloge;

    public DroitsVieePriveeService(List<SourceRenseignementsPersonnels> sources, DemandeVieePriveeRepository registre,
                                   AuditService audit, TirageHasard hasard, Clock horloge) {
        this.sources = sources.stream().sorted(Comparator.comparingInt(SourceRenseignementsPersonnels::ordre)).toList();
        this.registre = registre;
        this.audit = audit;
        this.hasard = hasard;
        this.horloge = horloge;
    }

    @Transactional
    public DossierRenseignements acces(String courriel, String demandeur) {
        String c = normaliser(courriel);
        Long tenant = TenantContext.get();
        DossierRenseignements dossier = new DossierRenseignements(c, horloge.instant(),
                sources.stream().map(s -> s.extraire(tenant, c)).toList());
        inscrire(DemandeVieePrivee.Type.ACCES, c, demandeur, null, dossier.total() + " renseignement(s) communiqué(s)");
        return dossier;
    }

    /** Efface la personne de toutes les sources, en une seule transaction : un refus d'une source annule tout. */
    @Transactional
    public List<BilanEffacement> effacer(String courriel, String motif, String demandeur) {
        String c = normaliser(courriel);
        if (c.equals(normaliser(demandeur))) {
            throw new EffacementRefuse("Une personne ne peut pas effacer son propre compte : la demande doit être traitée par un autre administrateur.");
        }
        Long tenant = TenantContext.get();
        Pseudonyme pseudonyme = Pseudonyme.nouveau(hasard.tirer());
        List<BilanEffacement> bilans = sources.stream().map(s -> s.effacer(tenant, c, pseudonyme)).toList();
        String resultat = bilans.stream().map(b -> b.source() + " : " + b.effaces() + " effacé(s), " + b.conserves() + " conservé(s)")
                .collect(Collectors.joining(" ; "));
        inscrire(DemandeVieePrivee.Type.EFFACEMENT, c, demandeur, motif, resultat);
        audit.log(demandeur, "LOI25_EFFACEMENT", "Personne", null, pseudonyme.valeur());
        return bilans;
    }

    public List<DemandeVieePrivee> registre() {
        return registre.findByTenantIdOrderByTraiteeLeDesc(TenantContext.get());
    }

    private void inscrire(DemandeVieePrivee.Type type, String courriel, String demandeur, String motif, String resultat) {
        registre.save(new DemandeVieePrivee(TenantContext.get(), type, empreinte(courriel), demandeur, motif, resultat, horloge.instant()));
    }

    static String normaliser(String courriel) {
        if (courriel == null || !courriel.contains("@")) throw new IllegalArgumentException("Courriel de la personne concernée invalide.");
        return courriel.trim().toLowerCase(Locale.ROOT);
    }

    static String empreinte(String courriel) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(courriel.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
