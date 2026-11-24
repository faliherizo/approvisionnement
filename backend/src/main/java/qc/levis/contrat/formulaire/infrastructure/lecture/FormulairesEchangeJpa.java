package qc.levis.contrat.formulaire.infrastructure.lecture;

import java.util.Optional;
import org.springframework.stereotype.Component;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.interop.application.port.FormulairesEchange;

/** Formulaires cibles des échanges hérités : identité et clés de champ de la dernière version. */
@Component
class FormulairesEchangeJpa implements FormulairesEchange {

    private final FormulaireRepository formulaires;
    private final FormulaireVersionRepository versions;
    private final LectureFormulairesJpa lecture;

    FormulairesEchangeJpa(FormulaireRepository formulaires, FormulaireVersionRepository versions, LectureFormulairesJpa lecture) {
        this.formulaires = formulaires;
        this.versions = versions;
        this.lecture = lecture;
    }

    @Override
    public Optional<FormulaireCible> parCode(Long tenantId, String code) {
        return code == null ? Optional.empty() : formulaires.findByTenantIdAndCode(tenantId, code.strip()).map(this::cible);
    }

    @Override
    public Optional<FormulaireCible> parId(Long tenantId, Long formulaireId) {
        return formulaires.findByIdAndTenantId(formulaireId, tenantId).map(this::cible);
    }

    private FormulaireCible cible(Formulaire f) {
        String structure = versions.findTopByFormulaireIdOrderByNumeroDesc(f.getId()).map(v -> v.getStructureJson()).orElse(null);
        return new FormulaireCible(f.getId(), f.getCode(), f.getNom(), lecture.libelles(structure).keySet());
    }
}
