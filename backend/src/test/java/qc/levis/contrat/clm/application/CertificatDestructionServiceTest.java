package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.CachetElectronique;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Certificat de destruction (addenda 3, Q48) : bordereau rendu en PDF puis scellé ; refusé tant que rien n'est détruit. */
class CertificatDestructionServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-17T12:00:00Z");
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final RenduDocuments rendu = mock(RenduDocuments.class);
    private final CachetElectronique cachet = mock(CachetElectronique.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(T0, ZoneOffset.UTC));
    private final CertificatDestructionService service = new CertificatDestructionService(trousses, rendu, cachet, ctx);

    private static final Map<String, Object> BORDEREAU = Map.of("reference", "TR-1", "titre", "Entente", "regle", "défaut 7 an(s) DETRUIRE",
            "echeance", "2025-03-01", "date", "2026-11-17T12:00:00Z", "autorite", Map.of("acteur", "admin@iq.example", "role", "Administration"),
            "methode", "Effacement", "resultat", "DETRUIT",
            "elements", List.of(Map.of("nom", "entente.pdf", "version", 1, "sha256", "abc")),
            "anonymises", Map.of("participants", 1, "invitations", 2, "decisions", 0, "analyses", 0));

    @Test
    void le_bordereau_devient_un_pdf_scelle() {
        Trousse t = new Trousse(3L, "TR-1", "Entente", "i@iq.example", T0);
        t.changerStatut(StatutTrousse.ANNULEE, T0);
        t.detruire(T0);
        t.consignerBordereau(ctx.ecrire(BORDEREAU));
        when(trousses.findByIdAndTenantId(1L, 3L)).thenReturn(Optional.of(t));
        when(rendu.genererPdf(eq("Certificat de destruction — TR-1"), any())).thenReturn(new byte[]{1});
        when(cachet.scellerPdf(new byte[]{1}, "Certificat de destruction TR-1")).thenReturn(new byte[]{2});
        assertArrayEquals(new byte[]{2}, portee.executerDans(3L, () -> service.certificat(1L)));
    }

    @Test
    void le_texte_nomme_elements_date_autorite_methode_et_resultat() {
        String texte = CertificatDestructionService.texte(BORDEREAU);
        for (String attendu : List.of("TR-1", "Type de contrat : —", "2026-11-17", "admin@iq.example — Administration", "Effacement",
                "Résultat : DETRUIT", "entente.pdf v1 — SHA-256 abc", "1 participant(s), 2 invitation(s)")) {
            assertTrue(texte.contains(attendu), attendu + " dans " + texte);
        }
    }

    @Test
    void sans_destruction_ni_trousse_aucun_certificat() {
        Trousse t = new Trousse(3L, "TR-2", "Ouverte", "i@iq.example", T0);
        when(trousses.findByIdAndTenantId(2L, 3L)).thenReturn(Optional.of(t));
        assertThrows(IllegalStateException.class, () -> portee.executerDans(3L, () -> service.certificat(2L)));
        assertThrows(NoSuchElementException.class, () -> portee.executerDans(3L, () -> service.certificat(9L)));
    }
}
