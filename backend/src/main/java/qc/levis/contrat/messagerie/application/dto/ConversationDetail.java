package qc.levis.contrat.messagerie.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Une conversation ouverte : ses participants et ses messages en clair, du plus ancien au plus récent. */
public record ConversationDetail(Long id, String sujet, String reference, Set<String> participants, List<MessageVue> messages) {
    public record MessageVue(Long id, String auteur, String texte, Instant envoyeLe) {}
}
