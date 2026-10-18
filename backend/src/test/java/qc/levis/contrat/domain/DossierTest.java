package qc.levis.contrat.domain;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.dossier.domain.Dossier;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires de l'entité de domaine {@link Dossier} (accesseurs et invariant de cycle de vie). */
class DossierTest {

    @Test
    void accesseurs_couvrentTousLesChamps() {
        Dossier d = new Dossier();
        Instant maintenant = Instant.now();
        d.setTitre("Acquisition");
        d.setNumeroAO("7000017905");
        d.setObjet("Solution de rédaction");
        d.setTypeAcquisition(Enums.TypeAcquisition.SERVICES_PROFESSIONNELS);
        d.setModeSollicitation(Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC);
        d.setStatut(Enums.StatutDossier.EN_COURS);
        d.setResponsable("Sarra");
        d.setCreePar("user@levis");
        d.setCreeLe(maintenant);
        d.setActif(false);
        d.setTenantId(7L);

        assertNull(d.getId());
        assertEquals("Acquisition", d.getTitre());
        assertEquals("7000017905", d.getNumeroAO());
        assertEquals("Solution de rédaction", d.getObjet());
        assertEquals(Enums.TypeAcquisition.SERVICES_PROFESSIONNELS, d.getTypeAcquisition());
        assertEquals(Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC, d.getModeSollicitation());
        assertEquals(Enums.StatutDossier.EN_COURS, d.getStatut());
        assertEquals("Sarra", d.getResponsable());
        assertEquals("user@levis", d.getCreePar());
        assertEquals(maintenant, d.getCreeLe());
        assertFalse(d.isActif());
        assertEquals(7L, d.getTenantId());
    }

    @Test
    void changerStatut_depuisEtatActif_reussit() {
        Dossier d = new Dossier();
        d.changerStatut(Enums.StatutDossier.ADJUGE);
        assertEquals(Enums.StatutDossier.ADJUGE, d.getStatut());
    }

    @Test
    void changerStatut_depuisCloture_refuse() {
        Dossier d = new Dossier();
        d.setStatut(Enums.StatutDossier.CLOTURE);
        assertThrows(IllegalStateException.class, () -> d.changerStatut(Enums.StatutDossier.OUVERT));
    }

    @Test
    void changerStatut_depuisAnnule_refuse() {
        Dossier d = new Dossier();
        d.setStatut(Enums.StatutDossier.ANNULE);
        assertThrows(IllegalStateException.class, () -> d.changerStatut(Enums.StatutDossier.OUVERT));
    }
}
