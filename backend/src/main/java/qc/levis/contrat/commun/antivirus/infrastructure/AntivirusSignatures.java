package qc.levis.contrat.commun.antivirus.infrastructure;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.antivirus.application.Antivirus;

/**
 * Antivirus de poste et de recette : ne reconnaît que la signature de test EICAR, que tout antivirus détecte. Il permet de
 * démontrer le refus sans service externe ; en exploitation, {@link AntivirusClamd} le remplace.
 */
@Component
@ConditionalOnProperty(name = "app.antivirus.mode", havingValue = "signatures", matchIfMissing = true)
public class AntivirusSignatures implements Antivirus {

    private static final byte[] EICAR = "EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(StandardCharsets.US_ASCII);

    @Override
    public boolean estSain(byte[] contenu) {
        for (int i = 0; i <= contenu.length - EICAR.length; i++) {
            if (correspond(contenu, i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean correspond(byte[] contenu, int debut) {
        for (int j = 0; j < EICAR.length; j++) {
            if (contenu[debut + j] != EICAR[j]) {
                return false;
            }
        }
        return true;
    }
}
