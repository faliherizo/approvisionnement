package qc.levis.contrat.saisie.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.saisie.application.port.RenduFiche;
import qc.levis.contrat.saisie.application.port.TransmissionDocument;
import qc.levis.contrat.saisie.domain.ContenuFiche;
import qc.levis.contrat.saisie.domain.Saisie;

/**
 * Document produit depuis les données d'une fiche, puis transmis (CHU 5.04). Le demandeur obtient le document de sa propre
 * fiche ; le personnel qui traite obtient celui de toute fiche de l'organisation.
 */
@Service
public class DocumentFicheService {

    private final AccesFiches acces;
    private final FormulaireRepository formulaires;
    private final FormulaireVersionRepository versions;
    private final RenduFiche rendu;
    private final TransmissionDocument transmission;
    private final AuditService audit;
    private final ObjectMapper json = new ObjectMapper();

    public DocumentFicheService(AccesFiches acces, FormulaireRepository formulaires, FormulaireVersionRepository versions,
                                RenduFiche rendu, TransmissionDocument transmission, AuditService audit) {
        this.acces = acces;
        this.formulaires = formulaires;
        this.versions = versions;
        this.rendu = rendu;
        this.transmission = transmission;
        this.audit = audit;
    }

    public byte[] pdf(Long formulaireId, Long ficheId, AccesFiches.Acteur acteur) {
        return rendu.pdf(contenu(formulaireId, ficheId, acteur));
    }

    public String transmettre(Long formulaireId, Long ficheId, String destinataire, String message, AccesFiches.Acteur acteur) {
        ContenuFiche c = contenu(formulaireId, ficheId, acteur);
        String statut = transmission.transmettre(TenantContext.get(), destinataire, c.formulaire() + " — " + c.numero(),
                message == null ? "" : message, c.numero() + ".pdf", rendu.pdf(c));
        audit.log(acteur.courriel(), "TRANSMISSION_DOCUMENT_FICHE", "Saisie", ficheId, c.numero() + " : " + statut);
        return statut;
    }

    ContenuFiche contenu(Long formulaireId, Long ficheId, AccesFiches.Acteur acteur) {
        Saisie s = acces.exigerFiche(formulaireId, ficheId, acteur);
        Formulaire f = formulaires.findByIdAndTenantId(formulaireId, TenantContext.get()).orElseThrow();
        String structure = versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId).map(v -> v.getStructureJson()).orElse("{}");
        return new ContenuFiche(f.getNom(), s.getNumero(), s.getStatut().name(), s.getSoumisLe(), s.getCreePar(),
                lignes(structure, s.getDonneesJson()));
    }

    /** Réponses dans l'ordre des pages et des champs du formulaire ; une question sans réponse figure, vide. */
    List<ContenuFiche.Ligne> lignes(String structure, String donnees) {
        JsonNode racine = lire(structure);
        JsonNode reponses = lire(donnees);
        List<ContenuFiche.Ligne> lignes = new ArrayList<>();
        for (JsonNode page : racine.path("pages")) {
            for (JsonNode c : page.path("champs")) {
                String cle = c.hasNonNull("cle") ? c.get("cle").asText() : c.path("code").asText();
                lignes.add(new ContenuFiche.Ligne(page.path("titre").asText(""), c.path("libelle").asText(cle), valeur(reponses.get(cle))));
            }
        }
        return lignes;
    }

    static String valeur(JsonNode v) {
        if (v == null || v.isNull() || (v.isTextual() && v.asText().isBlank())) return "—";
        if (v.isBoolean()) return v.asBoolean() ? "Oui" : "Non";
        if (v.isArray()) {
            List<String> items = new ArrayList<>();
            v.forEach(x -> items.add(x.isTextual() ? x.asText() : x.toString()));
            return String.join(", ", items);
        }
        return v.isTextual() ? v.asText() : v.toString();
    }

    private JsonNode lire(String texte) {
        try {
            return texte == null || texte.isBlank() ? json.createObjectNode() : json.readTree(texte);
        } catch (Exception illisible) {
            return json.createObjectNode();
        }
    }
}
