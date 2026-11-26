package qc.levis.contrat.clm.domain;

import java.util.Locale;

/**
 * Nature de la participation d'un participant de trousse (AF-48, RST-50) : elle borne ce qu'il peut faire au portail.
 * Un participant « consulter » lit les documents et atteste en avoir pris connaissance ; un participant « compléter »
 * remplit ses champs sans signer ; un participant « signer » signe. Sans précision, la nature est SIGNER, ce qui garde
 * aux trousses et aux intégrations antérieures leur comportement.
 */
public enum NatureParticipation {
    CONSULTER("consulter les documents"),
    COMPLETER("compléter vos champs"),
    SIGNER("signer");

    private final String permis;

    NatureParticipation(String permis) {
        this.permis = permis;
    }

    /** Nature saisie ; vide : SIGNER (rétrocompatible). */
    public static NatureParticipation de(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return SIGNER;
        }
        try {
            return valueOf(saisie.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nature de participation attendue : CONSULTER, COMPLETER ou SIGNER.");
        }
    }

    /** Une action qui n'est pas celle de la nature est refusée, quelle que soit l'interface qui la tente. */
    public void exiger(NatureParticipation action) {
        if (this != action) {
            throw new ActionHorsNature(this, action);
        }
    }

    /** Un participant « consulter » n'a aucun champ à remplir ; les autres remplissent ceux de leur rôle. */
    public boolean remplitDesChamps() {
        return this != CONSULTER;
    }

    /** Refus typé d'une action hors nature (ex. signer avec un lien de consultation). */
    public static final class ActionHorsNature extends IllegalStateException {

        private final NatureParticipation nature;
        private final NatureParticipation action;

        ActionHorsNature(NatureParticipation nature, NatureParticipation action) {
            super("Action refusée : votre participation vous permet de " + nature.permis + ", pas de " + action.permis + ".");
            this.nature = nature;
            this.action = action;
        }

        public NatureParticipation nature() {
            return nature;
        }

        public NatureParticipation action() {
            return action;
        }
    }
}
