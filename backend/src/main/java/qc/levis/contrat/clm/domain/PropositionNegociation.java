package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;

/**
 * Modification d'une clause proposée par la contrepartie (RST-55), puis décidée par une personne de l'organisation :
 * acceptée, refusée (motif requis) ou contre-proposée (texte requis). La suggestion de l'IA s'y attache sans jamais la
 * décider.
 */
@Entity
@Table(name = "clm_negociation_proposition", indexes = @Index(name = "ix_clm_nego_prop", columnList = "tenant_id,negociation_id"))
public class PropositionNegociation {

    public enum Decision {
        EN_ATTENTE, ACCEPTEE, REFUSEE, CONTRE_PROPOSEE;

        static final String ATTENDUE = "Décision attendue : ACCEPTEE, REFUSEE ou CONTRE_PROPOSEE.";

        /** Décision saisie ; une valeur inconnue est refusée (« en attente » l'est par l'arbitrage). */
        public static Decision de(String saisie) {
            try {
                return valueOf(saisie == null ? "" : saisie.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ATTENDUE, e);
            }
        }
    }

    /** Proposition de la contrepartie : le texte voulu pour la clause et, au besoin, son explication. */
    public record Saisie(String textePropose, String commentaire) {
        public Saisie {
            textePropose = textePropose == null ? "" : textePropose.strip();
            commentaire = commentaire == null || commentaire.isBlank() ? null : commentaire.strip();
            if (textePropose.isEmpty()) {
                throw new IllegalArgumentException("Texte proposé requis pour chaque clause modifiée.");
            }
        }
    }

    /** Décision d'une personne de l'organisation : un refus se motive, une contre-proposition se rédige. */
    public record Arbitrage(Decision decision, String contreTexte, String justification, String par) {
        public Arbitrage {
            if (decision == null || decision == Decision.EN_ATTENTE) {
                throw new IllegalArgumentException(Decision.ATTENDUE);
            }
            contreTexte = contreTexte == null ? "" : contreTexte.strip();
            justification = justification == null || justification.isBlank() ? null : justification.strip();
            if (decision == Decision.CONTRE_PROPOSEE && contreTexte.isEmpty()) {
                throw new IllegalArgumentException("Texte de la contre-proposition requis.");
            }
            if (decision == Decision.REFUSEE && justification == null) {
                throw new IllegalArgumentException("Motif du refus requis.");
            }
        }
    }

    /** Suggestion d'aide à la décision : position recommandée, résumé des écarts et sa source (modèle d'IA ou règles). */
    public record Suggestion(PositionNegociation position, String resume, String source) {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "negociation_id", nullable = false)
    private Long negociationId;

    @Column(nullable = false)
    private int tour;

    @Column(name = "cle_clause", nullable = false, length = 80)
    private String cleClause;

    @Column(name = "titre_clause", nullable = false, length = 250)
    private String titreClause;

    @Column(name = "texte_base", nullable = false, length = 20_000)
    private String texteBase;

    @Column(name = "texte_propose", nullable = false, length = 20_000)
    private String textePropose;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "proposee_le", nullable = false)
    private Instant proposeeLe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Decision decision = Decision.EN_ATTENTE;

    @Column(name = "texte_contre", length = 20_000)
    private String texteContre;

    @Column(length = 2000)
    private String justification;

    @Column(name = "decide_par", length = 150)
    private String decidePar;

    @Column(name = "decide_le")
    private Instant decideLe;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_position", length = 16)
    private PositionNegociation suggestionPosition;

    @Column(name = "suggestion_resume", length = 4000)
    private String suggestionResume;

    @Column(name = "suggestion_source", length = 120)
    private String suggestionSource;

    @Column(name = "suggeree_le")
    private Instant suggereeLe;

    protected PropositionNegociation() {
    }

    /** Une proposition qui laisse la clause intacte n'en est pas une. */
    public PropositionNegociation(Negociation n, ClauseNegociee base, Saisie saisie, Instant le) {
        if (saisie.textePropose().equals(base.texte())) {
            throw new IllegalArgumentException("La proposition ne modifie pas la clause « " + base.titre() + " ».");
        }
        this.tenantId = n.getTenantId();
        this.negociationId = n.getId();
        this.tour = n.getTour();
        this.cleClause = base.cle();
        this.titreClause = base.titre();
        this.texteBase = base.texte();
        this.textePropose = saisie.textePropose();
        this.commentaire = saisie.commentaire();
        this.proposeeLe = le;
    }

    public void decider(Arbitrage a, Instant le) {
        exigerEnAttente();
        this.decision = a.decision();
        this.texteContre = a.decision() == Decision.CONTRE_PROPOSEE ? a.contreTexte() : null;
        this.justification = a.justification();
        this.decidePar = a.par();
        this.decideLe = le;
    }

    /** La suggestion éclaire une décision à prendre : elle n'a plus d'objet une fois la proposition décidée. */
    public void suggerer(Suggestion s, Instant le) {
        exigerEnAttente();
        this.suggestionPosition = s.position();
        this.suggestionResume = s.resume();
        this.suggestionSource = s.source();
        this.suggereeLe = le;
    }

    /** Texte de la clause à l'issue du tour : la proposition acceptée, la contre-proposition, sinon le texte de départ. */
    public String texteRetenu() {
        return switch (decision) {
            case ACCEPTEE -> textePropose;
            case CONTRE_PROPOSEE -> texteContre;
            case REFUSEE, EN_ATTENTE -> texteBase;
        };
    }

    public boolean estDecidee() {
        return decision != Decision.EN_ATTENTE;
    }

    private void exigerEnAttente() {
        if (estDecidee()) {
            throw new PropositionDejaDecidee(titreClause);
        }
    }

    /** Refus typé : une décision prise ne se reprend pas ; un nouveau tour la remet en discussion. */
    public static final class PropositionDejaDecidee extends IllegalStateException {
        PropositionDejaDecidee(String clause) {
            super("La proposition sur « " + clause + " » est déjà décidée.");
        }
    }

    public Long getId() { return id; }
    public Long getNegociationId() { return negociationId; }
    public int getTour() { return tour; }
    public String getCleClause() { return cleClause; }
    public String getTitreClause() { return titreClause; }
    public String getTexteBase() { return texteBase; }
    public String getTextePropose() { return textePropose; }
    public String getCommentaire() { return commentaire; }
    public Instant getProposeeLe() { return proposeeLe; }
    public Decision getDecision() { return decision; }
    public String getTexteContre() { return texteContre; }
    public String getJustification() { return justification; }
    public String getDecidePar() { return decidePar; }
    public Instant getDecideLe() { return decideLe; }
    public PositionNegociation getSuggestionPosition() { return suggestionPosition; }
    public String getSuggestionResume() { return suggestionResume; }
    public String getSuggestionSource() { return suggestionSource; }
    public Instant getSuggereeLe() { return suggereeLe; }
}
