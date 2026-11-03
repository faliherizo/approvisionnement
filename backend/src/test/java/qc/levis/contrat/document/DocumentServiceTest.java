package qc.levis.contrat.document;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.document.application.DocumentService;
import qc.levis.contrat.document.application.dto.CommentaireRequest;
import qc.levis.contrat.document.application.repository.CommentaireDemandeRepository;
import qc.levis.contrat.document.application.repository.DocumentDemandeRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock DocumentDemandeRepository documents;
    @Mock CommentaireDemandeRepository commentaires;
    DocumentService service;

    @BeforeEach void avant() { service = new DocumentService(documents, commentaires); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    @Test void ajouter_refuse_un_fichier_vide() {
        assertThrows(IllegalArgumentException.class,
                () -> service.ajouter(1L, 2L, "x.pdf", "application/pdf", new byte[0], "interne", "u"));
        verify(documents, never()).save(any());
    }

    @Test void ajouter_refuse_un_fichier_trop_volumineux() {
        byte[] enorme = new byte[11 * 1024 * 1024];
        assertThrows(IllegalArgumentException.class,
                () -> service.ajouter(1L, 2L, "gros.bin", "application/octet-stream", enorme, "interne", "u"));
    }

    @Test void ajouter_enregistre_les_metadonnees_sans_exposer_le_binaire() {
        when(documents.save(any())).thenAnswer(i -> i.getArgument(0));
        var v = service.ajouter(1L, 2L, "contrat.pdf", "application/pdf", new byte[]{1, 2, 3}, "confidentiel", "admin");
        assertEquals("contrat.pdf", v.nomOriginal());
        assertEquals(3, v.tailleOctets());
        assertEquals("confidentiel", v.classification());
    }

    @Test void commentaire_vide_refuse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.ajouterCommentaire(2L, new CommentaireRequest("INTERNE", "  "), "u"));
    }

    @Test void commentaire_visibilite_inconnue_retombe_sur_interne() {
        when(commentaires.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("INTERNE", service.ajouterCommentaire(2L, new CommentaireRequest("BIZARRE", "bonjour"), "u").visibilite());
        assertEquals("DEMANDEUR", service.ajouterCommentaire(2L, new CommentaireRequest("DEMANDEUR", "bonjour"), "u").visibilite());
    }
}
