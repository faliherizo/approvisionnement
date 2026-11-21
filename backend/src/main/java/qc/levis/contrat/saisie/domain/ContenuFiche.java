package qc.levis.contrat.saisie.domain;

import java.time.Instant;
import java.util.List;

/** Ce que contient le document produit depuis une fiche (CHU 5.04) : son identification et ses réponses, dans l'ordre du formulaire. */
public record ContenuFiche(String formulaire, String numero, String statut, Instant soumisLe, String demandeur, List<Ligne> lignes) {

    public ContenuFiche {
        lignes = List.copyOf(lignes);
    }

    /** Une question du formulaire et la réponse donnée. */
    public record Ligne(String page, String libelle, String valeur) {}
}
