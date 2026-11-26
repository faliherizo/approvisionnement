package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Diffusion d'une transition de trousse aux systèmes externes concernés : retour de statut vers le dossier Salesforce
 * d'origine (ST-12), rappel au client d'intégration qui l'a créée (addenda 1), dépôt Microsoft 365 à l'achèvement
 * (Annexe 10, TI-01). Chaque {@link AbonneTransition} décide s'il est concerné.
 */
@Service
public class DiffusionStatut {

    private final List<AbonneTransition> abonnes;

    public DiffusionStatut(List<AbonneTransition> abonnes) {
        this.abonnes = List.copyOf(abonnes);
    }

    public void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees) {
        abonnes.forEach(a -> a.enfiler(t, evenement, sequence, donnees));
    }
}
