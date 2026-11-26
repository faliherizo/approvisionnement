package qc.levis.contrat.clm.application.dto;

import java.time.Instant;
import java.util.List;

/** Échanges de l'API de négociation assistée avec un tiers (RST-55, IA-05) : côté organisation et côté portail. */
public final class NegociationDto {

    private NegociationDto() {
    }

    public record OuvertureRequest(String nom, String courriel, String entreprise, String langue) {
    }

    public record ClauseVue(String cle, String titre, String texte) {
    }

    public record SuggestionVue(String position, String resume, String source, Instant le) {
    }

    public record PropositionVue(Long id, int tour, String cle, String titre, String texteBase, String textePropose, String commentaire,
                                 Instant proposeeLe, String decision, String texteContreProposition, String justification,
                                 String decidePar, Instant decideLe, SuggestionVue suggestion) {
    }

    public record VersionVue(int numero, String auteur, String motif, Instant creeLe, String empreinte, boolean integre) {
    }

    public record NegociationVue(Long id, String etat, String contrepartie, String courriel, String entreprise, int versionCourante,
                                 int tour, Instant expireLe, String ouvertePar, Instant ouverteLe, Instant termineeLe, String motifFin,
                                 List<ClauseVue> clauses, List<VersionVue> versions, List<PropositionVue> propositions,
                                 List<String> actions) {
    }

    public record DecisionRequest(Long propositionId, String decision, String texte, String justification) {
    }

    public record DecisionsRequest(List<DecisionRequest> decisions) {
    }

    public record AnnulationRequest(String motif) {
    }

    public record SegmentVue(String nature, String texte) {
    }

    public record EcartVue(String cle, String titre, String changement, List<SegmentVue> segments) {
    }

    public record ComparaisonVue(int de, int a, List<EcartVue> ecarts) {
    }

    public record PropositionRequest(String cle, String texte, String commentaire) {
    }

    public record PropositionsRequest(List<PropositionRequest> propositions) {
    }

    /** Vue de la contrepartie : jamais les suggestions internes ni l'identité de qui a décidé. */
    public record PortailVue(String reference, String titre, String organisation, String logo, String contrepartie, String langue,
                             String etat, int versionCourante, Instant expireLe, List<ClauseVue> clauses,
                             List<PropositionVue> propositions, List<Integer> versions) {
    }
}
