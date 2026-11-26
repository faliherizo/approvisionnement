package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.application.repository.CleOrganisationRepository;
import qc.levis.contrat.clm.domain.CleEnveloppee;
import qc.levis.contrat.clm.domain.CleOrganisation;

/** Registre des clés d'organisation (ADR-037) : première clé, rotation, réenveloppement après rotation de la clé maîtresse. */
class RegistreClesOrganisationTest {

    private static final Instant T0 = Instant.parse("2026-11-26T12:00:00Z");

    private final CleOrganisationRepository cles = mock(CleOrganisationRepository.class);
    private final GestionnaireCles kms = mock(GestionnaireCles.class);
    private final AleaCryptographique alea = n -> new byte[n];
    private final ContexteClm ctx = mock(ContexteClm.class);
    private final RegistreClesOrganisation registre = new RegistreClesOrganisation(cles, kms, alea, ctx);

    @BeforeEach
    void preparer() {
        when(ctx.maintenant()).thenReturn(T0);
        when(kms.envelopper(any())).thenReturn(new CleEnveloppee("k2", "neuve"));
        when(kms.cleMaitresseActive()).thenReturn("k2");
        when(cles.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void la_premiere_ecriture_cree_la_version_1_enveloppee_par_le_kms() {
        when(cles.findByTenantIdAndStatut(5L, CleOrganisation.Statut.ACTIVE)).thenReturn(Optional.empty());
        CleOrganisation c = registre.active(5L);
        assertEquals(1, c.getVersion());
        assertEquals("k2", c.getCleMaitresse());
        verify(kms).envelopper(new byte[RegistreClesOrganisation.OCTETS_CLE]);
    }

    @Test
    void une_rotation_sans_cle_active_cree_la_version_suivante() {
        when(cles.findByTenantIdAndStatut(5L, CleOrganisation.Statut.ACTIVE)).thenReturn(Optional.empty());
        when(cles.findByTenantIdOrderByVersionDesc(5L)).thenReturn(List.of());
        assertEquals(1, registre.pivoter(5L).getVersion());
        assertEquals("k2", registre.cleMaitresseActive());
    }

    @Test
    void la_rotation_de_la_cle_maitresse_reenveloppe_seulement_les_cles_des_autres_maitresses() {
        CleOrganisation ancienne = CleOrganisation.creer(5L, 1, new CleEnveloppee("k1", "vieille"), T0);
        CleOrganisation courante = CleOrganisation.creer(5L, 2, new CleEnveloppee("k2", "ok"), T0);
        when(cles.findByTenantIdOrderByVersionDesc(5L)).thenReturn(List.of(courante, ancienne));
        when(kms.desenvelopper(new CleEnveloppee("k1", "vieille"))).thenReturn(new byte[]{7});
        assertEquals(1, registre.reenvelopper(5L));
        assertEquals(new CleEnveloppee("k2", "neuve"), ancienne.enveloppe());
        assertEquals(new CleEnveloppee("k2", "ok"), courante.enveloppe());
        verify(cles).save(ancienne);
        verify(cles, never()).save(courante);
    }
}
