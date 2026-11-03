package qc.levis.contrat.securite.application;

import qc.levis.contrat.securite.domain.Niveau;

/**
 * Décision PURE de masquage/exportabilité selon la classification d'un champ et l'habilitation de
 * l'utilisateur : un champ dont le niveau dépasse l'habilitation est masqué (et non exportable).
 */
public final class Masquage {

    private Masquage() {}

    public static boolean estMasque(String niveauChamp, String habilitation) {
        return Niveau.depuis(niveauChamp).ordinal() > Niveau.depuis(habilitation).ordinal();
    }

    public static boolean exportable(String niveauChamp, String habilitation) {
        return !estMasque(niveauChamp, habilitation);
    }
}
