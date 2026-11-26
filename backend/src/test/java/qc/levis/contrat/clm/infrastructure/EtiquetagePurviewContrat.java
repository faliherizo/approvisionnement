package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.EtiquetagePurview;
import qc.levis.contrat.clm.domain.EtiquettePurview;

/**
 * Contrat du port {@link EtiquetagePurview} (ADR-036), exécuté contre chaque adaptateur : le catalogue offre des étiquettes
 * des deux types ; une apposition réussit sur un fichier déposé ; sans fichier, elle échoue par son résultat, jamais par
 * exception.
 */
abstract class EtiquetagePurviewContrat {

    static final Long TENANT = 7L;

    abstract EtiquetagePurview sujet();

    /** Adresse d'un fichier que l'adaptateur sait trouver. */
    abstract String fichierDepose();

    @Test
    void le_catalogue_offre_des_etiquettes_de_sensibilite_et_de_retention() {
        List<EtiquettePurview> c = sujet().catalogue(TENANT);
        assertTrue(c.stream().anyMatch(e -> e.type() == EtiquettePurview.Type.SENSIBILITE));
        assertTrue(c.stream().anyMatch(e -> e.type() == EtiquettePurview.Type.RETENTION));
    }

    @Test
    void les_etiquettes_du_catalogue_s_apposent_au_fichier_depose() {
        List<EtiquettePurview> c = sujet().catalogue(TENANT);
        EtiquettePurview s = c.stream().filter(e -> e.type() == EtiquettePurview.Type.SENSIBILITE).findFirst().orElseThrow();
        EtiquettePurview r = c.stream().filter(e -> e.type() == EtiquettePurview.Type.RETENTION).findFirst().orElseThrow();
        assertTrue(sujet().apposer(TENANT, fichierDepose(), s, r).succes());
        assertTrue(sujet().apposer(TENANT, fichierDepose(), s, null).succes(), "sensibilité seule");
        assertTrue(sujet().apposer(TENANT, fichierDepose(), null, r).succes(), "rétention seule");
    }

    @Test
    void sans_fichier_depose_l_apposition_echoue_par_son_resultat() {
        EtiquettePurview s = EtiquettePurview.sensibilite("x", "X");
        EtiquetagePurview.Resultat r = sujet().apposer(TENANT, null, s, null);
        assertFalse(r.succes());
        assertNotNull(r.message());
        assertFalse(sujet().apposer(TENANT, " ", s, null).succes());
    }
}
