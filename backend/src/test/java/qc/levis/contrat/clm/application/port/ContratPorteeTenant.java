package qc.levis.contrat.clm.application.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Contrat du port {@link PorteeTenant}, exécuté contre chacune de ses implémentations : un traitement voit son tenant,
 * puis le tenant précédent est rétabli, y compris après une erreur. L'isolation multi-tenant en dépend.
 */
public abstract class ContratPorteeTenant {

    /** Une portée neuve, sans tenant courant. */
    protected abstract PorteeTenant portee();

    @Test
    void hors_traitement_aucun_tenant_n_est_courant() {
        assertNull(portee().courant());
    }

    @Test
    void le_traitement_voit_son_tenant_puis_le_precedent_est_retabli() {
        PorteeTenant p = portee();
        List<Long> vus = new ArrayList<>();
        p.executerDans(7L, () -> {
            vus.add(p.courant());
            p.executerDans(9L, () -> vus.add(p.courant()));
            return vus.add(p.courant());
        });
        assertEquals(Arrays.asList(7L, 9L, 7L), vus);
        assertNull(p.courant());
    }

    @Test
    void le_tenant_precedent_est_retabli_meme_apres_une_erreur() {
        PorteeTenant p = portee();
        assertThrows(IllegalStateException.class, () -> p.executerDans(7L, () -> {
            throw new IllegalStateException("panne");
        }));
        assertNull(p.courant());
    }

    @Test
    void le_resultat_du_traitement_est_rendu() {
        assertEquals("ok", portee().executerDans(7L, () -> "ok"));
    }
}
