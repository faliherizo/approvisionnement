package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.Preremplissage;

/**
 * Décisions des cas d'utilisation testables sans persistance : champ masqué par sa condition à la génération,
 * archive de preuve jamais livrée partielle, montant attendu de l'analyse IA lu tel que saisi ou ignoré.
 */
class DecisionsServicesClmTest {

    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new PorteeTenantMemoire(), new ObjectMapper(),
            Clock.systemUTC());

    @Test
    void un_champ_dont_la_condition_est_fausse_est_masque_dans_le_contrat_genere() {
        ModeleContrat m = new ModeleContrat(1L, "cle", 1, "Convention", "auteur@x.ca", Instant.EPOCH);
        m.modifier("Convention", null, null, "Montant {{montant}} ; garantie {{garantie}}.", new ModeleContrat.Contenu("""
                [{"cle":"montant","libelle":"Montant","type":"TEXTE"},
                 {"cle":"garantie","libelle":"Garantie","type":"TEXTE",
                  "conditions":[{"champ":"montant","operateur":"SUP","valeur":"100000"}]}]
                """, "[]", "[]"), Instant.EPOCH);

        ClmDto.ApercuModele petit = DocumentClmService.assembler(m, Map.of("montant", "500", "garantie", "Caution"), ctx);
        assertEquals(List.of("garantie"), petit.champsMasques());
        assertEquals("Montant 500 ; garantie [garantie].", petit.texte(), "la valeur saisie d'un champ masqué n'apparaît pas");

        ClmDto.ApercuModele grand = DocumentClmService.assembler(m, Map.of("montant", "250000", "garantie", "Caution"), ctx);
        assertTrue(grand.champsMasques().isEmpty());
        assertEquals("Montant 250000 ; garantie Caution.", grand.texte());
    }

    @Test
    void l_archive_de_preuve_contient_chaque_fichier_dans_l_ordre() throws IOException {
        Map<String, byte[]> fichiers = new LinkedHashMap<>();
        fichiers.put("documents/contrat.pdf", "%PDF".getBytes(StandardCharsets.US_ASCII));
        fichiers.put("manifeste.json", "{}".getBytes(StandardCharsets.US_ASCII));
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        PreuveService.archiver(fichiers, sortie);

        List<String> noms = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(sortie.toByteArray()))) {
            for (ZipEntry e = zip.getNextEntry(); e != null; e = zip.getNextEntry()) {
                noms.add(e.getName());
            }
        }
        assertEquals(List.of("documents/contrat.pdf", "manifeste.json"), noms);
    }

    @Test
    void une_ecriture_interrompue_ne_livre_jamais_d_archive_partielle() {
        OutputStream disquePlein = new OutputStream() {
            @Override
            public void write(int octet) throws IOException {
                throw new IOException("disque plein");
            }
        };
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> PreuveService.archiver(Map.of("journal.json", new byte[]{1}), disquePlein));
        assertEquals("Archive de preuve impossible", e.getMessage());
    }

    @Test
    void le_montant_attendu_est_lu_tel_que_saisi_ou_ignore_s_il_est_absent_ou_illisible() {
        assertNull(AnalyseService.montantAttendu(null));
        assertNull(AnalyseService.montantAttendu(new Preremplissage.Valeur(null, Preremplissage.Provenance.MANUEL, null)));
        assertEquals(new BigDecimal("1234.50"),
                AnalyseService.montantAttendu(new Preremplissage.Valeur("1 234,50", Preremplissage.Provenance.MANUEL, null)));
        assertEquals(new BigDecimal("250000.00"),
                AnalyseService.montantAttendu(new Preremplissage.Valeur(new BigDecimal("250000.00"), Preremplissage.Provenance.SALESFORCE, null)));
        assertNull(AnalyseService.montantAttendu(new Preremplissage.Valeur("environ mille", Preremplissage.Provenance.MANUEL, null)));
    }

    @Test
    void un_refus_d_acces_sans_identite_est_journalise_comme_anonyme() {
        AuditService audit = mock(AuditService.class);
        JournalClm journal = new JournalClm(mock(EvenementTrousseRepository.class), audit, ctx);
        journal.refus(null, "GET /api/clm/trousses", "Accès refusé.");
        journal.refus(" ", "GET /api/clm/trousses", "Accès refusé.");
        journal.refus("a@x.ca", "GET /api/clm/admin/journal", "Access Denied");
        verify(audit, times(2)).log("anonyme", "CLM_ACCES_REFUSE", "API", null, "GET /api/clm/trousses · Accès refusé.");
        verify(audit).log("a@x.ca", "CLM_ACCES_REFUSE", "API", null, "GET /api/clm/admin/journal · Access Denied");
    }
}
