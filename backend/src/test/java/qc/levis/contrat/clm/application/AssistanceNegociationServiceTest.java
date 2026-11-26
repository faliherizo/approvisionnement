package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.ConseillerNegociation;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PositionNegociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.commun.module.application.ModuleNonSouscrit;

/**
 * RST-55 : sans l'option IA d'analyse, la liste « Clause attendue » n'est pas lisible ; la suggestion repose alors sur le seul
 * écart au texte normatif, sans erreur pour la personne.
 */
class AssistanceNegociationServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-26T10:00:00Z");

    private final RegistreNegociation registre = mock(RegistreNegociation.class);
    private final LectureTrousse lecture = mock(LectureTrousse.class);
    private final ListesClm listes = mock(ListesClm.class);
    private final JournalClm journal = mock(JournalClm.class);
    private final ConseillerNegociation conseiller = demande -> Optional.empty();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new PorteeTenantMemoire(), new ObjectMapper(),
            Clock.fixed(T0, ZoneOffset.UTC));
    private final AssistanceNegociationService service = new AssistanceNegociationService(registre, mock(VuesNegociation.class), lecture,
            conseiller, listes, journal, ctx);

    @Test
    void sans_l_option_ia_d_analyse_la_suggestion_ne_mesure_que_l_ecart_au_texte_normatif() {
        Negociation n = new Negociation(new Negociation.Ouverture(7L, 9L, new Negociation.Contrepartie("Agrinord", "a@b.ca", null, Langue.FR),
                "h", T0.plusSeconds(60), "x", T0));
        ClauseNegociee conf = new ClauseNegociee("conf", "Confidentialité", "Les informations restent confidentielles.");
        PropositionNegociation p = new PropositionNegociation(n, conf, new PropositionNegociation.Saisie("Les informations sont publiques.", null), T0);
        Habilitations.Acteur acteur = mock(Habilitations.Acteur.class);
        Trousse t = mock(Trousse.class);
        when(registre.exiger(5L)).thenReturn(n);
        when(lecture.exigerGestion(any(), eq(acteur))).thenReturn(t);
        when(registre.clauses(n, 1)).thenReturn(List.of());
        when(registre.duTour(n)).thenReturn(List.of(p));
        when(listes.motsCles(ListesClm.Liste.CLAUSE_ATTENDUE)).thenThrow(new ModuleNonSouscrit("clm_ia"));

        service.suggerer(5L, acteur);

        assertEquals(PositionNegociation.CONTRE_PROPOSER, p.getSuggestionPosition(), "aucun élément attendu à défendre : l'écart seul compte");
        assertEquals(AssistanceNegociationService.SOURCE_REGLES, p.getSuggestionSource());
        assertTrue(p.getSuggestionResume().startsWith("Écart sur « Confidentialité »"));
        verify(journal).consigner(eq(t), eq("NEGOCIATION_SUGGESTION"), any(), any());
    }
}
