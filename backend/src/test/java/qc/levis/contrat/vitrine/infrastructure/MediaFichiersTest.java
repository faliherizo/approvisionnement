package qc.levis.contrat.vitrine.infrastructure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import qc.levis.contrat.vitrine.application.port.MediaStore;

/** Médiathèque sur disque : écrit, liste et lit sous sa racine seulement. */
class MediaFichiersTest {

    @TempDir Path racine;

    @Test
    void ecrit_liste_et_lit_sous_sa_racine() throws Exception {
        MediaFichiers m = new MediaFichiers(racine.toString());
        assertEquals(List.of(), new MediaFichiers(racine.resolve("absent").toString()).lister(""), "racine absente : rien");
        m.enregistrer("vitrines/iq/presentation.mp4", new byte[]{1, 2, 3});
        m.enregistrer("aide/index.json", "[]".getBytes());

        assertEquals(List.of(new MediaStore.Media("vitrines/iq/presentation.mp4", 3)), m.lister("vitrines/"));
        assertEquals(2, m.lister("").size());
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(m.fichier("vitrines/iq/presentation.mp4").orElseThrow()));
        assertTrue(m.fichier("vitrines/iq").isEmpty(), "un répertoire n'est pas un fichier");
        assertTrue(m.fichier("absent.mp4").isEmpty());
        assertTrue(m.fichier(null).isEmpty());
    }

    @Test
    void rien_ne_sort_de_la_mediatheque() throws Exception {
        Files.writeString(racine.getParent().resolve("secret-mediafichiers.txt"), "x");
        MediaFichiers m = new MediaFichiers(racine.toString());
        assertTrue(m.fichier("../secret-mediafichiers.txt").isEmpty());
        assertTrue(m.fichier(" ").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> m.enregistrer("../evasion.mp4", new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> m.enregistrer("", new byte[]{1}));
    }
}
