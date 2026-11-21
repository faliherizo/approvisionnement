package qc.levis.contrat.messagerie.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Jusqu'où un participant a lu une conversation : ce qui est plus récent est « non lu ». */
@Entity
@Table(name = "messagerie_lecture", uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "participant"}))
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "conversation_id", nullable = false, updatable = false) private Long conversationId;
    @Column(nullable = false, updatable = false, length = 150) private String participant;
    @Column(nullable = false) private Instant luJusqua;

    protected Lecture() { }

    public Lecture(Long conversationId, String participant, Instant luJusqua) {
        this.conversationId = conversationId;
        this.participant = Conversation.normaliser(participant);
        this.luJusqua = luJusqua;
    }

    public void avancer(Instant le) {
        if (le.isAfter(luJusqua)) luJusqua = le;
    }

    public Long getConversationId() { return conversationId; }
    public String getParticipant() { return participant; }
    public Instant getLuJusqua() { return luJusqua; }
}
