package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Branchement Hibernate du chiffrement au repos (ADR-037) : une propriété renommée est signalée, jamais ignorée. */
class ChiffrementAuReposTest {

    @Test
    void une_propriete_absente_du_document_est_une_erreur_de_configuration() {
        String[] proprietes = {"nom", ChiffrementAuRepos.CONTENU, ChiffrementAuRepos.VERSION};
        assertEquals(1, ChiffrementAuRepos.indice(proprietes, ChiffrementAuRepos.CONTENU));
        assertEquals(2, ChiffrementAuRepos.indice(proprietes, ChiffrementAuRepos.VERSION));
        assertThrows(IllegalStateException.class, () -> ChiffrementAuRepos.indice(new String[]{"nom"}, ChiffrementAuRepos.CONTENU));
    }
}
