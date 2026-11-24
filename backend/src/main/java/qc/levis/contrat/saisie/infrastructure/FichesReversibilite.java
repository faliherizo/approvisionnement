package qc.levis.contrat.saisie.infrastructure;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.reversibilite.application.SourceReversibilite;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;

/** Fiches (soumissions) de l'organisation, données saisies comprises, pour la réversibilité (CHU 6.06). */
@Component
public class FichesReversibilite implements SourceReversibilite {

    private final SaisieRepository saisies;

    public FichesReversibilite(SaisieRepository saisies) {
        this.saisies = saisies;
    }

    @Override
    public String module() {
        return "saisie";
    }

    @Override
    public Map<String, List<?>> jeuxDeDonnees(Long tenant) {
        return Map.of("fiches.json", saisies.findByTenantIdOrderByIdAsc(tenant));
    }
}
