package qc.levis.contrat.commun.antivirus.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.antivirus.application.Antivirus;

/**
 * Antivirus ClamAV par le protocole publié du démon clamd (ADR-027) : commande {@code zINSTREAM}, contenu en blocs
 * préfixés de leur longueur (4 octets, gros-boutiste), bloc vide final ; réponse {@code stream: OK} ou
 * {@code stream: <signature> FOUND}. Toute autre issue (refus de connexion, délai, erreur de clamd) rend l'analyse
 * impossible : le fichier n'est pas accepté.
 */
@Component
@ConditionalOnProperty(name = "app.antivirus.mode", havingValue = "clamav")
public class AntivirusClamd implements Antivirus {

    private static final Logger log = LoggerFactory.getLogger(AntivirusClamd.class);
    private static final int BLOC = 64 * 1024;

    private final String hote;
    private final int port;
    private final int delaiMs;

    public AntivirusClamd(@Value("${app.antivirus.clamav.hote}") String hote,
                          @Value("${app.antivirus.clamav.port:3310}") int port,
                          @Value("${app.antivirus.clamav.delai-ms:30000}") int delaiMs) {
        if (hote == null || hote.isBlank()) {
            throw new IllegalStateException("Antivirus ClamAV : ANTIVIRUS_CLAMAV_HOTE est requis.");
        }
        this.hote = hote.strip();
        this.port = port;
        this.delaiMs = delaiMs;
    }

    @Override
    public boolean estSain(byte[] contenu) {
        String reponse = analyser(contenu);
        if (reponse.endsWith("OK")) {
            return true;
        }
        if (reponse.endsWith("FOUND")) {
            log.warn("Maliciel détecté par ClamAV : {}", reponse);
            return false;
        }
        throw new AntivirusIndisponible("Analyse antivirus impossible : " + reponse);
    }

    private String analyser(byte[] contenu) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(hote, port), delaiMs);
            s.setSoTimeout(delaiMs);
            DataOutputStream sortie = new DataOutputStream(s.getOutputStream());
            sortie.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            for (int i = 0; i < contenu.length; i += BLOC) {
                int n = Math.min(BLOC, contenu.length - i);
                sortie.writeInt(n);
                sortie.write(contenu, i, n);
            }
            sortie.writeInt(0);
            sortie.flush();
            return lire(s.getInputStream());
        } catch (IOException e) {
            log.warn("ClamAV injoignable ({}:{}) : {}", hote, port, e.getClass().getSimpleName());
            throw new AntivirusIndisponible("Analyse antivirus impossible : service injoignable.");
        }
    }

    private static String lire(InputStream entree) throws IOException {
        ByteArrayOutputStream tampon = new ByteArrayOutputStream();
        for (int b; (b = entree.read()) > 0; ) {
            tampon.write(b);
        }
        return tampon.toString(StandardCharsets.US_ASCII).strip();
    }
}
