package qc.levis.contrat.clm.application.port;

import java.util.List;
import qc.levis.contrat.clm.domain.EvenementSiem;

/** Journal d'audit d'une organisation lu par curseur, dans l'ordre d'écriture (Annexe 10, TI-04). */
public interface JournalSecurite {

    List<EvenementSiem> depuis(Long tenantId, long curseur, int limite);
}
