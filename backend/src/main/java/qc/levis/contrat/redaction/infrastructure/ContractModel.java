package qc.levis.contrat.redaction.infrastructure;

import java.util.List;
import qc.levis.contrat.soumission.domain.Bordereau;

/**
 * Modèle riche d'un contrat formel, indépendant du format de sortie (PDF/DOCX).
 * Produit par {@link ContractComposer}, consommé par les {@link DocumentRenderer}.
 */
public record ContractModel(
        String titre,
        String numeroAO,
        String type,
        String statut,
        String date,
        String organismePublic,
        String prestataire,
        String contexte,
        List<Section> sections,
        List<Annexe> annexes,
        Bordereau bordereau,
        GabaritRendu gabarit) {

    /** Normalise le gabarit (jamais nul → gabarit par défaut). */
    public ContractModel {
        if (gabarit == null) gabarit = GabaritRendu.defaut();
    }

    /** Constructeur rétro-compatible (sans gabarit) : conserve l'export « contrat » historique. */
    public ContractModel(String titre, String numeroAO, String type, String statut, String date,
                         String organismePublic, String prestataire, String contexte,
                         List<Section> sections, List<Annexe> annexes, Bordereau bordereau) {
        this(titre, numeroAO, type, statut, date, organismePublic, prestataire, contexte,
                sections, annexes, bordereau, GabaritRendu.defaut());
    }

    /** Section éditoriale regroupant des articles (ex. « Attestations »). */
    public record Section(String titre, List<Article> articles) {}

    /** Article numéroté hiérarchiquement (ex. « 10.06.01 »). */
    public record Article(String numero, String titre, String texte) {}

    /** Annexe (issue d'une annexe déclarée ou d'un addenda émis). */
    public record Annexe(String numero, String objet, List<String> elements) {}

    /** Bordereau de prix : lignes tarifaires + total. */
    public record Bordereau(String titre, List<Ligne> lignes, String total) {
        public record Ligne(String description, String quantite, String taux, String total) {}
    }

    /** Vrai si aucune information de préambule (parties/contexte) n'est présente. */
    public boolean preambuleVide() {
        return vide(organismePublic) && vide(prestataire) && vide(contexte);
    }

    public boolean aBordereau() { return bordereau != null && !bordereau.lignes().isEmpty(); }

    private static boolean vide(String s) { return s == null || s.isBlank(); }
}
