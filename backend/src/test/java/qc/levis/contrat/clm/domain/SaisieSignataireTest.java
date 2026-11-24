package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** ST-05, ST-09 : ce que le signataire saisit au portail est contrôlé avant la signature. */
class SaisieSignataireTest {

    private static Modele.Champ champ(String cle, Modele.TypeChamp type, boolean obligatoire, String format, List<String> options) {
        return new Modele.Champ(cle, "Libellé " + cle, type, obligatoire, format, options, null, null, null, "SIGNATAIRE_1", null);
    }

    private static final List<Modele.Champ> CHAMPS = List.of(
            champ("titre", Modele.TypeChamp.TEXTE, true, null, null),
            champ("telephone", Modele.TypeChamp.TEXTE, false, "\\+?\\d{10,11}", null),
            champ("province", Modele.TypeChamp.LISTE, false, " ", List.of("QC", "ON")),
            champ("note", Modele.TypeChamp.TEXTE, false, "", null));

    @Test
    void les_valeurs_valides_sont_retenues_rognees_et_les_facultatifs_vides_ignores() {
        Map<String, String> saisis = new HashMap<>(Map.of("titre", "  Directrice  ", "telephone", "+15145550101", "province", "QC",
                "hors-champ", "ignoré"));
        saisis.put("note", " ");
        Map<String, String> r = SaisieSignataire.valider(CHAMPS, saisis);
        assertEquals(Map.of("titre", "Directrice", "telephone", "+15145550101", "province", "QC"), r);
    }

    @Test
    void toutes_les_erreurs_sont_rendues_ensemble() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> SaisieSignataire.valider(CHAMPS,
                Map.of("telephone", "514-pas-un-numero", "province", "Ailleurs")));
        assertTrue(e.getMessage().contains("Champ obligatoire : Libellé titre"));
        assertTrue(e.getMessage().contains("Format invalide : Libellé telephone"));
        assertTrue(e.getMessage().contains("Valeur non permise : Libellé province"));
    }

    @Test
    void un_obligatoire_blanc_est_refuse_et_un_texte_libre_sans_format_est_admis() {
        assertThrows(IllegalArgumentException.class, () -> SaisieSignataire.valider(CHAMPS, Map.of("titre", "   ")));
        assertEquals("Texte libre", SaisieSignataire.valider(CHAMPS, Map.of("titre", "x", "note", "Texte libre")).get("note"));
        assertTrue(SaisieSignataire.valider(List.of(), Map.of("x", "y")).isEmpty());
    }
}
