package qc.levis.contrat.commun.module;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.module.application.ModuleDependances;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;

/** Tests purs de la résolution des dépendances de modules (sans Spring ni BD). */
class ModuleDependancesTest {

    private ModuleCatalogue m(String code, String requiert) {
        return new ModuleCatalogue(code, code, "F", 0, false, requiert);
    }

    private final List<ModuleCatalogue> catalogue = List.of(
            m("formulaire", null),
            m("regle", "formulaire"),
            m("saisie", "formulaire"),
            m("notification", "saisie"),
            m("rapport", "saisie"),
            m("interop", "saisie"),
            m("securite_avancee", null));

    @Test
    void parse_gere_null_blanc_et_espaces() {
        assertTrue(ModuleDependances.parse(null).isEmpty());
        assertTrue(ModuleDependances.parse("  ").isEmpty());
        assertEquals(List.of("a", "b", "c"), ModuleDependances.parse("a, b ,c"));
    }

    @Test
    void requis_transitifs_remonte_toute_la_chaine() {
        Set<String> r = ModuleDependances.requisTransitifs(catalogue, "notification");
        assertEquals(Set.of("notification", "saisie", "formulaire"), r);
    }

    @Test
    void requis_transitifs_module_sans_prerequis() {
        assertEquals(Set.of("formulaire"), ModuleDependances.requisTransitifs(catalogue, "formulaire"));
    }

    @Test
    void dependants_transitifs_descend_tous_les_dependants() {
        Set<String> d = ModuleDependances.dependantsTransitifs(catalogue, "formulaire");
        assertEquals(Set.of("formulaire", "regle", "saisie", "notification", "rapport", "interop"), d);
    }

    @Test
    void dependants_transitifs_feuille() {
        assertEquals(Set.of("notification"), ModuleDependances.dependantsTransitifs(catalogue, "notification"));
    }

    @Test
    void resolution_robuste_aux_cycles() {
        List<ModuleCatalogue> cyclique = List.of(m("a", "b"), m("b", "a"));
        assertEquals(Set.of("a", "b"), ModuleDependances.requisTransitifs(cyclique, "a"));
        assertEquals(Set.of("a", "b"), ModuleDependances.dependantsTransitifs(cyclique, "a"));
    }
}
