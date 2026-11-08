package qc.levis.contrat.clm.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.domain.AnalyseContrat;

/**
 * Listes de référence du module : types de contrat, unités, rôles dans le contrat, clauses attendues, champs
 * Salesforce et priorités des tâches assignées. La plateforme en fournit les valeurs par défaut ; l'organisation les adapte dans son administration.
 */
@Service
public class ListesClm {

    public enum Liste {
        TYPE_CONTRAT("CLM_TYPE_CONTRAT", "Type de contrat"),
        UNITE("CLM_UNITE", "Unité administrative"),
        ROLE_CONTRAT("CLM_ROLE_CONTRAT", "Rôle dans le contrat"),
        CLAUSE_ATTENDUE("CLM_CLAUSE_ATTENDUE", "Clause attendue"),
        CHAMP_SALESFORCE("CLM_CHAMP_SALESFORCE", "Champ Salesforce"),
        PRIORITE_TACHE("CLM_PRIORITE_TACHE", "Priorité de tâche");

        private final String code;
        private final String libelle;

        Liste(String code, String libelle) {
            this.code = code;
            this.libelle = libelle;
        }

        public String code() {
            return code;
        }

        public String libelle() {
            return libelle;
        }
    }

    private final ReferencesClm references;

    public ListesClm(ReferencesClm references) {
        this.references = references;
    }

    public List<ReferencesClm.Valeur> valeurs(Liste liste) {
        return references.valeurs(liste.code());
    }

    /**
     * Code retenu pour une saisie : celui de la liste de l'organisation, sans égard à la casse. Une saisie vide n'en
     * retient aucun ; une liste vide laisse la saisie libre, l'organisation n'ayant rien imposé.
     */
    public String exiger(Liste liste, String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return null;
        }
        String valeur = saisie.trim();
        List<ReferencesClm.Valeur> valeurs = valeurs(liste);
        if (valeurs.isEmpty()) {
            return valeur;
        }
        return valeurs.stream().map(ReferencesClm.Valeur::code).filter(c -> c.equalsIgnoreCase(valeur)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "« " + valeur + " » ne figure pas dans la liste « " + liste.libelle() + " » de l'organisation."));
    }

    /** Mots-clés par code (option IA) : le détail de chaque valeur. */
    public Map<String, List<String>> motsCles(Liste liste) {
        Map<String, List<String>> parCode = new LinkedHashMap<>();
        valeurs(liste).forEach(v -> parCode.put(v.code(), AnalyseContrat.motsCles(v.detail())));
        return parCode;
    }
}
