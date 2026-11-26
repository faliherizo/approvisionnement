package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.ConseillerNegociation;
import qc.levis.contrat.clm.domain.PositionNegociation;
import qc.levis.contrat.ia.application.AiTraceabiliteService;
import qc.levis.contrat.ia.application.dto.AiRedactionRequest;
import qc.levis.contrat.ia.application.dto.AiRedactionResponse;

/** RST-55 (ADR-038) : l'IA locale conseille si elle le peut ; désactivée, en panne ou muette, elle laisse la place aux règles. */
class ConseillerNegociationOllamaTest {

    private static final ConseillerNegociation.Demande DEMANDE = new ConseillerNegociation.Demande("Paiement",
            "Paiement à 30 jours.", "Paiement à 30 jours.", "Paiement à 90 jours.", "Trésorerie serrée");

    private final AiTraceabiliteService encadrement = mock(AiTraceabiliteService.class);
    private final AtomicReference<AiRedactionRequest> recue = new AtomicReference<>();

    private ConseillerNegociationOllama conseiller(AiRedactionResponse reponse) {
        return new ConseillerNegociationOllama(req -> {
            recue.set(req);
            return reponse;
        }, encadrement);
    }

    @Test
    void l_ia_activee_rend_son_resume_et_la_position_qu_elle_nomme() {
        when(encadrement.estActivee()).thenReturn(true);
        Optional<ConseillerNegociation.Avis> avis = conseiller(new AiRedactionResponse(" Délai triplé.\nPOSITION : REFUSER ", "llama3.1"))
                .conseiller(DEMANDE);
        assertEquals("Délai triplé.\nPOSITION : REFUSER", avis.orElseThrow().resume());
        assertEquals(Optional.of(PositionNegociation.REFUSER), avis.orElseThrow().position());
        assertEquals("llama3.1", avis.orElseThrow().modele());
        assertEquals(ConseillerNegociationOllama.INSTRUCTION, recue.get().instruction());
        assertEquals("Paiement à 90 jours.", recue.get().texteExistant());
        assertTrue(recue.get().contexte().contains("Texte normatif (bibliothèque) :\nPaiement à 30 jours."));
        assertTrue(recue.get().contexte().endsWith("Commentaire du tiers : Trésorerie serrée"));
    }

    @Test
    void desactivee_muette_ou_en_panne_l_ia_ne_rend_aucun_avis() {
        when(encadrement.estActivee()).thenReturn(false);
        assertEquals(Optional.empty(), conseiller(new AiRedactionResponse("x", "m")).conseiller(DEMANDE));
        assertEquals(null, recue.get(), "désactivée pour l'organisation : aucune donnée n'est soumise");
        when(encadrement.estActivee()).thenReturn(true);
        assertEquals(Optional.empty(), conseiller(null).conseiller(DEMANDE));
        assertEquals(Optional.empty(), conseiller(new AiRedactionResponse(null, "m")).conseiller(DEMANDE));
        assertEquals(Optional.empty(), conseiller(new AiRedactionResponse("  ", "m")).conseiller(DEMANDE));
        ConseillerNegociationOllama enPanne = new ConseillerNegociationOllama(req -> {
            throw new IllegalStateException("connexion refusée");
        }, encadrement);
        assertEquals(Optional.empty(), enPanne.conseiller(DEMANDE));
    }

    @Test
    void le_contexte_omet_le_commentaire_absent() {
        String contexte = ConseillerNegociationOllama.contexte(new ConseillerNegociation.Demande("Durée", "12 mois", "12 mois", "24 mois", null));
        assertFalse(contexte.contains("Commentaire"));
        assertTrue(contexte.startsWith("Clause : Durée\n"));
    }
}
