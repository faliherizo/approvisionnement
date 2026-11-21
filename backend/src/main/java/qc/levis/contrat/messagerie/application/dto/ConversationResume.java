package qc.levis.contrat.messagerie.application.dto;

import java.time.Instant;
import java.util.Set;

/** Une conversation dans la liste de la personne : sujet, participants, dernier message et messages non lus. */
public record ConversationResume(Long id, String sujet, String reference, Set<String> participants, Instant dernierMessageLe, long nonLus) {}
