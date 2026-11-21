package qc.levis.contrat.messagerie.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Message d'une conversation : texte brut, chiffré au repos, jamais modifié après l'envoi (la conversation fait trace).
 * Le chiffrement est l'affaire de la couche application : l'entité ne connaît que le texte chiffré.
 */
@Entity
@Table(name = "messagerie_message")
public class Message {

    public static final int LONGUEUR_MAX = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long conversationId;
    @Column(nullable = false, length = 150) private String auteur;
    @Column(nullable = false, updatable = false, length = 20000) private String contenuChiffre;
    @Column(nullable = false, updatable = false) private Instant envoyeLe;

    protected Message() { }

    public Message(Long conversationId, String auteur, String contenuChiffre, Instant envoyeLe) {
        this.conversationId = conversationId;
        this.auteur = Conversation.normaliser(auteur);
        this.contenuChiffre = contenuChiffre;
        this.envoyeLe = envoyeLe;
    }

    /** Le texte clair est validé avant chiffrement : non vide, de longueur raisonnable. */
    public static String exigerTexte(String texte) {
        if (texte == null || texte.isBlank()) throw new IllegalArgumentException("Le message est vide.");
        if (texte.length() > LONGUEUR_MAX) throw new IllegalArgumentException("Le message dépasse " + LONGUEUR_MAX + " caractères.");
        return texte.strip();
    }

    /** Loi 25 : l'auteur effacé devient un pseudonyme ; le message reste dans la conversation des autres participants. */
    public void pseudonymiserAuteur(String pseudonyme) {
        auteur = pseudonyme;
    }

    public Long getId() { return id; }
    public Long getConversationId() { return conversationId; }
    public String getAuteur() { return auteur; }
    public String getContenuChiffre() { return contenuChiffre; }
    public Instant getEnvoyeLe() { return envoyeLe; }
}
