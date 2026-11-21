package qc.levis.contrat.saisie.application;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.saisie.application.dto.SaisieVue;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;

/**
 * Soumission d'une fiche avec contrôle des places par choix (CHU 3.06), autour de {@link SaisieService#soumettre} qui
 * reste inchangé. Le formulaire est verrouillé pour toute la transaction : {@code SaisieService.soumettre} s'y joint, et
 * deux personnes ne peuvent pas prendre ensemble la dernière place d'un choix ni le dernier rang de répondant.
 */
@Service
public class SoumissionFiche {

    private final SaisieService saisies;
    private final SaisieRepository fiches;
    private final FormulaireRepository formulaires;
    private final PlacesService places;
    private final com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();

    public SoumissionFiche(SaisieService saisies, SaisieRepository fiches, FormulaireRepository formulaires, PlacesService places) {
        this.saisies = saisies;
        this.fiches = fiches;
        this.formulaires = formulaires;
        this.places = places;
    }

    @Transactional
    public SaisieVue soumettre(Long ficheId, SoumissionRequest req, String ip) {
        Saisie s = fiches.findByIdAndTenantId(ficheId, TenantContext.get())
                .orElseThrow(() -> new NoSuchElementException("Fiche introuvable : " + ficheId));
        formulaires.verrouiller(s.getFormulaireId(), TenantContext.get());
        String donnees = req.donneesJson() != null ? req.donneesJson() : s.getDonneesJson();
        places.plan(s.getFormulaireId(), s.getId()).exigerDesPlaces(reponses(donnees));
        return saisies.soumettre(ficheId, req, ip);
    }

    private Map<String, Object> reponses(String donnees) {
        try {
            return donnees == null || donnees.isBlank() ? Map.of()
                    : json.readValue(donnees, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { });
        } catch (Exception illisible) {
            return Map.of();
        }
    }
}
