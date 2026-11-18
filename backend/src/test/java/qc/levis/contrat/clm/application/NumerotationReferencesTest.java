package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import qc.levis.contrat.clm.application.repository.CompteurReferenceRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.CompteurReference;

/** Numérotation des trousses sous concurrence (essai de 500 utilisateurs, addenda 3 Q38). */
class NumerotationReferencesTest {

    private final CompteurReferenceRepository compteurs = mock(CompteurReferenceRepository.class);
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final NumerotationReferences.Initialisation initialisation = new NumerotationReferences.Initialisation(compteurs, trousses);
    private final NumerotationReferences numerotation = new NumerotationReferences(compteurs, trousses, initialisation);

    @Test
    void le_compteur_verrouille_donne_le_numero_suivant_et_saute_une_reference_saisie_a_la_main() {
        CompteurReference c = new CompteurReference(3L, "TR", 2026, 7);
        when(compteurs.findByTenantIdAndPrefixeAndAnnee(3L, "TR", 2026)).thenReturn(Optional.of(c));
        when(trousses.existsByTenantIdAndReference(3L, "TR-2026-00008")).thenReturn(true);
        assertEquals("TR-2026-00009", numerotation.suivante(3L, "TR", 2026));
        verify(compteurs).save(c);
        verify(compteurs, never()).saveAndFlush(any());
    }

    @Test
    void une_annee_neuve_part_des_references_deja_attribuees() {
        CompteurReference cree = new CompteurReference(3L, "TR", 2027, 12);
        when(compteurs.findByTenantIdAndPrefixeAndAnnee(3L, "TR", 2027)).thenReturn(Optional.empty(), Optional.of(cree));
        when(trousses.countByTenantIdAndReferenceStartingWith(3L, "TR-2027-")).thenReturn(12L);
        assertEquals("TR-2027-00013", numerotation.suivante(3L, "TR", 2027));
        verify(compteurs).saveAndFlush(argThat(n -> n.getDernier() == 12));
    }

    @Test
    void un_compteur_cree_au_meme_instant_par_une_autre_requete_est_reutilise() {
        CompteurReference concurrent = new CompteurReference(3L, "TR", 2026, 40);
        when(compteurs.findByTenantIdAndPrefixeAndAnnee(3L, "TR", 2026)).thenReturn(Optional.empty(), Optional.of(concurrent));
        when(compteurs.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_clm_compteur_reference"));
        assertEquals("TR-2026-00041", numerotation.suivante(3L, "TR", 2026), "la violation reste hors de la transaction appelante");
    }
}
