package qc.levis.contrat.vieprivee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.vieprivee.application.DroitsVieePriveeService;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.application.repository.DemandeVieePriveeRepository;
import qc.levis.contrat.vieprivee.domain.*;

class DroitsVieePriveeServiceTest {

    static final Instant T = Instant.parse("2026-11-20T12:00:00Z");
    final List<String> ordreEffacement = new ArrayList<>();
    DemandeVieePriveeRepository registre = mock(DemandeVieePriveeRepository.class);
    AuditService audit = mock(AuditService.class);
    DroitsVieePriveeService service;

    /** Source factice qui note l'ordre dans lequel elle est effacée. */
    SourceRenseignementsPersonnels source(String nom, int ordre, int elements) {
        return new SourceRenseignementsPersonnels() {
            @Override public int ordre() { return ordre; }
            @Override public ExtraitRenseignements extraire(Long t, String c) {
                return new ExtraitRenseignements(nom, "d", "e",
                        java.util.Collections.nCopies(elements, Renseignement.de(nom, T, "courriel", c)));
            }
            @Override public BilanEffacement effacer(Long t, String c, Pseudonyme p) {
                ordreEffacement.add(nom);
                return BilanEffacement.complet(nom, elements);
            }
        };
    }

    @BeforeEach
    void avant() {
        TenantContext.set(7L);
        service = new DroitsVieePriveeService(List.of(source("Compte", 100, 1), source("Journal", 0, 2)), registre, audit,
                () -> UUID.fromString("00000000-0000-0000-0000-000000000001"), Clock.fixed(T, ZoneOffset.UTC));
    }

    @AfterEach
    void apres() { TenantContext.clear(); }

    @Test
    void l_acces_interroge_chaque_source_et_inscrit_la_demande_sous_empreinte() {
        DossierRenseignements d = service.acces("  Marie@Levis.QC.ca ", "admin@levis.qc.ca");
        assertEquals("marie@levis.qc.ca", d.courriel());
        assertEquals(T, d.produitLe());
        assertEquals(3, d.total());
        ArgumentCaptor<DemandeVieePrivee> c = ArgumentCaptor.forClass(DemandeVieePrivee.class);
        verify(registre).save(c.capture());
        assertEquals(DemandeVieePrivee.Type.ACCES, c.getValue().getType());
        assertEquals(7L, c.getValue().getTenantId());
        assertEquals(64, c.getValue().getEmpreinteCourriel().length());
        assertFalse(c.getValue().getEmpreinteCourriel().contains("marie"));
        assertEquals("3 renseignement(s) communiqué(s)", c.getValue().getResultat());
    }

    @Test
    void l_effacement_passe_par_toutes_les_sources_le_compte_en_dernier() {
        List<BilanEffacement> bilans = service.effacer("marie@levis.qc.ca", "Demande 12", "admin@levis.qc.ca");
        assertEquals(List.of("Journal", "Compte"), ordreEffacement);
        assertEquals(2, bilans.size());
        ArgumentCaptor<DemandeVieePrivee> c = ArgumentCaptor.forClass(DemandeVieePrivee.class);
        verify(registre).save(c.capture());
        assertEquals("Journal : 2 effacé(s), 0 conservé(s) ; Compte : 1 effacé(s), 0 conservé(s)", c.getValue().getResultat());
        assertEquals("Demande 12", c.getValue().getMotif());
        verify(audit).log("admin@levis.qc.ca", "LOI25_EFFACEMENT", "Personne", null, "personne-effacee-00000000-000");
    }

    @Test
    void on_ne_s_efface_pas_soi_meme_et_le_courriel_doit_etre_valide() {
        assertThrows(EffacementRefuse.class, () -> service.effacer("ADMIN@levis.qc.ca", "m", "admin@levis.qc.ca"));
        assertThrows(IllegalArgumentException.class, () -> service.acces("sans-arobase", "admin"));
        assertThrows(IllegalArgumentException.class, () -> service.acces(null, "admin"));
        verifyNoInteractions(registre);
        assertTrue(ordreEffacement.isEmpty());
    }

    @Test
    void le_registre_est_celui_de_l_organisation_courante() {
        service.registre();
        verify(registre).findByTenantIdOrderByTraiteeLeDesc(7L);
    }
}
