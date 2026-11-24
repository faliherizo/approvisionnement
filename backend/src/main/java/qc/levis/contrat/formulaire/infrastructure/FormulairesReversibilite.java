package qc.levis.contrat.formulaire.infrastructure;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.reversibilite.application.SourceReversibilite;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;

/** Formulaires de l'organisation et toutes leurs versions (structure, règles), pour la réversibilité (CHU 6.06). */
@Component
public class FormulairesReversibilite implements SourceReversibilite {

    private final FormulaireRepository formulaires;
    private final FormulaireVersionRepository versions;

    public FormulairesReversibilite(FormulaireRepository formulaires, FormulaireVersionRepository versions) {
        this.formulaires = formulaires;
        this.versions = versions;
    }

    @Override
    public String module() {
        return "formulaire";
    }

    @Override
    public Map<String, List<?>> jeuxDeDonnees(Long tenant) {
        List<Formulaire> liste = formulaires.findByTenantIdOrderByNomAsc(tenant);
        Map<String, List<?>> jeux = new LinkedHashMap<>();
        jeux.put("formulaires.json", liste);
        jeux.put("formulaire-versions.json", liste.stream()
                .flatMap(f -> versions.findByFormulaireIdOrderByNumeroDesc(f.getId()).stream()).toList());
        return jeux;
    }
}
