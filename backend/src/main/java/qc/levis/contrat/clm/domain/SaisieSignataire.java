package qc.levis.contrat.clm.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Valeurs que le signataire saisit au portail dans les champs qui lui sont assignés (ST-05, ST-09) : un champ
 * obligatoire vide, une valeur hors format ou hors liste est refusé, et toutes les erreurs sont rendues ensemble. Les
 * valeurs retenues sont rognées ; un champ facultatif laissé vide n'est pas retenu. Une saisie hors des champs assignés
 * est ignorée : le signataire ne remplit que ce qui lui revient.
 */
public final class SaisieSignataire {

    private SaisieSignataire() {
    }

    public static Map<String, String> valider(List<Modele.Champ> assignes, Map<String, String> saisis) {
        Map<String, String> retenus = new HashMap<>();
        List<String> erreurs = new ArrayList<>();
        for (Modele.Champ c : assignes) {
            String v = saisis.get(c.cle());
            boolean vide = v == null || v.isBlank();
            if (vide && c.obligatoire()) {
                erreurs.add("Champ obligatoire : " + c.libelle());
            } else if (!vide && c.format() != null && !c.format().isBlank() && !Pattern.compile(c.format()).matcher(v).matches()) {
                erreurs.add("Format invalide : " + c.libelle());
            } else if (!vide && c.type() == Modele.TypeChamp.LISTE && !c.options().contains(v)) {
                erreurs.add("Valeur non permise : " + c.libelle());
            } else if (!vide) {
                retenus.put(c.cle(), v.trim());
            }
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        return retenus;
    }
}
