package qc.levis.contrat.clm.application;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.JournalSecurite;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.EvenementSiem;

/**
 * Accès machine au journal d'audit pour le SIEM de l'organisation (Annexe 10, TI-04 ; ADR-033). Un client d'intégration
 * de type SIEM, authentifié par jeton (OAuth 2.0 client credentials, ADR-011), lit les événements de son organisation à
 * partir d'un curseur, page par page, sans session utilisateur ; révoquer le client coupe la collecte.
 */
@Service
public class JournalSiemService {

    public static final int LIMITE_MAX = 500;

    private final JournalSecurite journal;

    public JournalSiemService(JournalSecurite journal) {
        this.journal = journal;
    }

    public ClmDto.FluxSiem lire(ClientIntegration client, long apres, int limite) {
        if (!client.lecteurJournal()) {
            throw new AccessDeniedException("Lecture du journal réservée à un client d'intégration de type SIEM.");
        }
        int taille = Math.max(1, Math.min(limite, LIMITE_MAX));
        List<EvenementSiem> page = journal.depuis(client.getTenantId(), Math.max(apres, 0), taille);
        long suivant = page.isEmpty() ? Math.max(apres, 0) : page.get(page.size() - 1).id();
        return new ClmDto.FluxSiem(page.stream().map(EvenementSiem::asim).toList(), page.stream().map(EvenementSiem::cef).toList(),
                suivant, page.size() == taille);
    }
}
