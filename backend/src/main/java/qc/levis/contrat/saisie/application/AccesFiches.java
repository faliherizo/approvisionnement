package qc.levis.contrat.saisie.application;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;

/**
 * Contrôle d'accès aux fiches (OWASP A01, IDOR) : le formulaire doit être de l'organisation, la fiche de ce formulaire,
 * et la personne doit être soit le demandeur, soit du personnel qui traite. Une fiche hors de portée est introuvable :
 * on ne révèle pas qu'elle existe.
 */
@Service
public class AccesFiches {

    static final Set<String> PERSONNEL = Set.of("ROLE_ADMIN", "ROLE_APPRO", "ROLE_DIRECTION", "ROLE_SUPERADMIN");

    /** La personne qui agit et ses rôles (« ROLE_… »). */
    public record Acteur(String courriel, Collection<String> roles) {
        public boolean estPersonnel() {
            return roles.stream().anyMatch(PERSONNEL::contains);
        }
    }

    private final FormulaireRepository formulaires;
    private final SaisieRepository fiches;

    public AccesFiches(FormulaireRepository formulaires, SaisieRepository fiches) {
        this.formulaires = formulaires;
        this.fiches = fiches;
    }

    public void exigerFormulaire(Long formulaireId) {
        formulaires.findByIdAndTenantId(formulaireId, TenantContext.get())
                .orElseThrow(() -> new NoSuchElementException("Formulaire introuvable : " + formulaireId));
    }

    public Saisie exigerFiche(Long formulaireId, Long ficheId, Acteur acteur) {
        Saisie s = fiches.findByIdAndTenantId(ficheId, TenantContext.get())
                .filter(f -> Objects.equals(f.getFormulaireId(), formulaireId))
                .orElseThrow(() -> new NoSuchElementException("Fiche introuvable : " + ficheId));
        if (!acteur.estPersonnel() && !estDemandeur(s, acteur)) throw new NoSuchElementException("Fiche introuvable : " + ficheId);
        return s;
    }

    public boolean estDemandeurDe(Long formulaireId, Long ficheId, Acteur acteur) {
        return fiches.findByIdAndTenantId(ficheId, TenantContext.get())
                .filter(f -> Objects.equals(f.getFormulaireId(), formulaireId)).map(f -> estDemandeur(f, acteur)).orElse(false);
    }

    static boolean estDemandeur(Saisie s, Acteur acteur) {
        return s.getCreePar() != null && s.getCreePar().equalsIgnoreCase(acteur.courriel());
    }
}
