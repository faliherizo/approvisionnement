package qc.levis.contrat.clm.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import qc.levis.contrat.clm.application.PortailNegociationService;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.commun.module.application.ModuleService;

/** RST-55 : l'organisation qui n'a pas souscrit l'option de négociation n'expose rien par le lien, quelle que soit l'opération. */
class PortailNegociationControllerTest {

    private final PortailNegociationService portail = mock(PortailNegociationService.class);
    private final ModuleService modules = mock(ModuleService.class);
    private final PortailNegociationController controleur = new PortailNegociationController(portail, modules);

    @Test
    void sans_l_option_souscrite_chaque_operation_du_portail_est_refusee() {
        when(portail.organisation("j")).thenReturn(7L);
        when(modules.estActif(7L, PortailNegociationController.OPTION)).thenReturn(false);
        AccessDeniedException e = assertThrows(AccessDeniedException.class, () -> controleur.consulter("j"));
        assertTrue(e.getMessage().contains("option"));
        assertThrows(AccessDeniedException.class, () -> controleur.comparer("j", 1, 2));
        assertThrows(AccessDeniedException.class, () -> controleur.proposer("j", new NegociationDto.PropositionsRequest(null)));
        assertThrows(AccessDeniedException.class, () -> controleur.accepter("j"));
        verify(portail, org.mockito.Mockito.times(4)).organisation("j");
        verifyNoMoreInteractions(portail);
    }
}
