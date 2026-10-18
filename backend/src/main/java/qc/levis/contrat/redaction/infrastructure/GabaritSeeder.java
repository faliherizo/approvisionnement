package qc.levis.contrat.redaction.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.redaction.application.repository.GabaritRepository;
import qc.levis.contrat.redaction.domain.CorpsGabarit;
import qc.levis.contrat.redaction.domain.Gabarit;
import qc.levis.contrat.soumission.domain.Bordereau;

/**
 * Amorce le catalogue global des gabarits normalisés SCT (tenant_id nul) au premier
 * démarrage. Les tenants héritent de ce catalogue et peuvent le surcharger. Idempotent.
 */
@Component
@Order(5)
public class GabaritSeeder implements CommandLineRunner {

    private final GabaritRepository gabarits;

    public GabaritSeeder(GabaritRepository gabarits) {
        this.gabarits = gabarits;
    }

    @Override
    public void run(String... args) {
        if (gabarits.countByTenantIdIsNull() > 0) return;
        int ordre = 0;
        creer("CONTRAT_SERVICES", "Contrat de services", "APPEL D'OFFRES - SERVICES", "CONTRAT",
                "Services de nature technique", "CONTRAT", CorpsGabarit.CONTRAT, ordre++);
        creer("REGIE", "Régie", "APPEL D'OFFRES - SERVICES", "RÉGIE",
                "Services de nature technique", "RÉGIE", CorpsGabarit.CONTRAT, ordre++);
        creer("DEVIS_TECHNIQUE", "Devis technique", "DEVIS TECHNIQUE", "DESCRIPTION DES BESOINS",
                "Services de nature technique", "DEVIS", CorpsGabarit.CONTRAT, ordre++);
        creer("FORMULAIRE_SOUMISSION", "Formulaire de soumission", "APPEL D'OFFRES - SERVICES",
                "FORMULAIRE DE SOUMISSION", "Services de nature technique", "FORMULAIRE DE SOUMISSION",
                CorpsGabarit.CONTRAT, ordre++);
        creer("AVIS_INTERET", "Avis d'intérêt", "AVIS D'INTÉRÊT / DEMANDE D'INFORMATION", "AVIS D'INTÉRÊT",
                null, "AVIS D'INTÉRÊT", CorpsGabarit.CONTRAT, ordre++);
        creer("CCAG", "CCAG", "APPEL D'OFFRES EN TECHNOLOGIES DE L'INFORMATION",
                "CAHIER DES CLAUSES ADMINISTRATIVES GÉNÉRALES", "Contrat de services", "CCAG",
                CorpsGabarit.CONTRAT, ordre++);
        creer("BORDEREAU_PRIX", "Bordereau de prix", "APPEL D'OFFRES - SERVICES", "BORDEREAU DE PRIX",
                null, "BORDEREAU DE PRIX", CorpsGabarit.BORDEREAU, ordre);
    }

    private void creer(String code, String nom, String surLabel, String label, String sousTitre,
                       String piedLabel, CorpsGabarit corps, int ordre) {
        Gabarit g = new Gabarit();
        g.setTenantId(null);
        g.setCode(code);
        g.setNom(nom);
        g.setSurLabel(surLabel);
        g.setLabel(label);
        g.setSousTitre(sousTitre);
        g.setPiedLabel(piedLabel);
        g.setCorps(corps);
        g.setOrdre(ordre);
        g.setActif(true);
        gabarits.save(g);
    }
}
