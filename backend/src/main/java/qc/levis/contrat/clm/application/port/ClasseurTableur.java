package qc.levis.contrat.clm.application.port;

import java.util.List;

/** Classeur Excel (XLSX) d'une feuille, pour l'export des rapports (Annexe 10, AF-37) ; le format reste à l'infrastructure. */
public interface ClasseurTableur {

    byte[] xlsx(String feuille, List<String> entetes, List<List<String>> lignes);
}
