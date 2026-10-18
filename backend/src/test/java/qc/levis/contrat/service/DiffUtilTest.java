package qc.levis.contrat.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.application.DiffUtil;
import static org.junit.jupiter.api.Assertions.*;

class DiffUtilTest {

    @Test
    void textesIdentiques_toutEgal() {
        List<DiffUtil.Ligne> d = DiffUtil.diff("a\nb", "a\nb");
        assertTrue(d.stream().allMatch(l -> l.type() == DiffUtil.Type.EGAL));
    }

    @Test
    void ajoutDeLigne() {
        List<DiffUtil.Ligne> d = DiffUtil.diff("a", "a\nb");
        assertTrue(d.stream().anyMatch(l -> l.type() == DiffUtil.Type.AJOUT && l.texte().equals("b")));
    }

    @Test
    void suppressionDeLigne() {
        List<DiffUtil.Ligne> d = DiffUtil.diff("a\nb", "a");
        assertTrue(d.stream().anyMatch(l -> l.type() == DiffUtil.Type.SUPPRESSION && l.texte().equals("b")));
    }

    @Test
    void textesNuls_gerentBien() {
        assertNotNull(DiffUtil.diff(null, null));
        assertFalse(DiffUtil.diff(null, "x").isEmpty());
    }
}
