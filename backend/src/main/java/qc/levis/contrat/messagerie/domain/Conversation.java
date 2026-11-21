package qc.levis.contrat.messagerie.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Conversation de la messagerie interne (CHU 5.05) entre intervenants d'une même organisation. Seuls ses participants la
 * voient : pour toute autre personne, elle n'existe pas.
 */
@Entity
@Table(name = "messagerie_conversation")
public class Conversation {

    public static final int PARTICIPANTS_MAX = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long tenantId;
    @Column(nullable = false, length = 200) private String sujet;
    @Column(length = 100) private String reference;
    @Column(nullable = false, length = 150) private String creePar;
    @Column(nullable = false, updatable = false) private Instant creeLe;
    @Column(nullable = false) private Instant dernierMessageLe;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "messagerie_participant", joinColumns = @JoinColumn(name = "conversation_id"))
    @Column(name = "courriel", length = 150)
    private Set<String> participants = new LinkedHashSet<>();

    protected Conversation() { }

    public Conversation(Long tenantId, String sujet, String reference, String creePar, Instant le) {
        if (sujet == null || sujet.isBlank()) throw new IllegalArgumentException("Le sujet de la conversation est requis.");
        this.tenantId = tenantId;
        this.sujet = sujet.trim();
        this.reference = reference == null || reference.isBlank() ? null : reference.trim();
        this.creePar = normaliser(creePar);
        this.creeLe = le;
        this.dernierMessageLe = le;
        participants.add(this.creePar);
    }

    public static String normaliser(String courriel) {
        return courriel.trim().toLowerCase(Locale.ROOT);
    }

    public boolean estParticipant(String courriel) {
        return courriel != null && participants.contains(normaliser(courriel));
    }

    /** Une personne qui n'y participe pas ne doit même pas apprendre que la conversation existe. */
    public void exigerParticipant(String courriel) {
        if (!estParticipant(courriel)) throw new NoSuchElementException("Conversation introuvable : " + id);
    }

    public void ajouterParticipant(String courriel) {
        if (participants.size() >= PARTICIPANTS_MAX && !estParticipant(courriel)) {
            throw new IllegalArgumentException("Une conversation compte au plus " + PARTICIPANTS_MAX + " participants.");
        }
        participants.add(normaliser(courriel));
    }

    /** Loi 25 : la personne effacée n'est plus nommée parmi les participants. */
    public void pseudonymiserParticipant(String courriel, String pseudonyme) {
        if (participants.remove(normaliser(courriel))) participants.add(pseudonyme);
        if (creePar.equals(normaliser(courriel))) creePar = pseudonyme;
    }

    public void messageRecu(Instant le) {
        dernierMessageLe = le;
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getSujet() { return sujet; }
    public String getReference() { return reference; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getDernierMessageLe() { return dernierMessageLe; }
    public Set<String> getParticipants() { return Set.copyOf(participants); }
}
