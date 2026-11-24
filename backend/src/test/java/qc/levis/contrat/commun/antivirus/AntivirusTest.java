package qc.levis.contrat.commun.antivirus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.antivirus.application.Antivirus;
import qc.levis.contrat.commun.antivirus.infrastructure.AntivirusClamd;
import qc.levis.contrat.commun.antivirus.infrastructure.AntivirusSignatures;

/**
 * CHU 7.08 : les deux adaptateurs respectent le même contrat (sain, infecté) ; l'adaptateur ClamAV parle le protocole
 * INSTREAM de clamd, que le faux démon ci-dessous lit comme le vrai, et refuse dès que l'analyse n'aboutit pas.
 */
class AntivirusTest {

    private static final byte[] EICAR =
            "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes(StandardCharsets.US_ASCII);

    private ServerSocket clamd;
    private final AtomicReference<String> commande = new AtomicReference<>();
    private final AtomicReference<byte[]> recu = new AtomicReference<>();

    @AfterEach
    void arreter() throws IOException {
        if (clamd != null) clamd.close();
    }

    /** Faux clamd : lit la commande et les blocs, puis répond comme clamd selon le contenu reçu. */
    private AntivirusClamd clamd(String reponseForcee) throws IOException {
        clamd = new ServerSocket(0);
        Thread t = new Thread(() -> {
            try (Socket s = clamd.accept()) {
                DataInputStream in = new DataInputStream(s.getInputStream());
                ByteArrayOutputStream cmd = new ByteArrayOutputStream();
                for (int b; (b = in.read()) > 0; ) cmd.write(b);
                commande.set(cmd.toString(StandardCharsets.US_ASCII));
                ByteArrayOutputStream contenu = new ByteArrayOutputStream();
                for (int n; (n = in.readInt()) > 0; ) contenu.write(in.readNBytes(n));
                recu.set(contenu.toByteArray());
                String reponse = reponseForcee != null ? reponseForcee
                        : new AntivirusSignatures().estSain(contenu.toByteArray()) ? "stream: OK" : "stream: Win.Test.EICAR_HDB-1 FOUND";
                s.getOutputStream().write((reponse + "\0").getBytes(StandardCharsets.US_ASCII));
            } catch (IOException ignore) {
                // le client a fermé : rien à répondre
            }
        });
        t.setDaemon(true);
        t.start();
        return new AntivirusClamd("127.0.0.1", clamd.getLocalPort(), 2000);
    }

    @Test
    void clamd_fichierSain_selonLeProtocoleInstream() throws IOException {
        byte[] gros = new byte[150_000];
        assertThat(clamd(null).estSain(gros)).isTrue();
        assertThat(commande.get()).isEqualTo("zINSTREAM");
        assertThat(recu.get()).hasSize(150_000);
    }

    @Test
    void clamd_maliciel_detecte() throws IOException {
        assertThat(clamd(null).estSain(EICAR)).isFalse();
    }

    @Test
    void clamd_erreur_rendLAnalyseImpossible() throws IOException {
        var a = clamd("INSTREAM size limit exceeded. ERROR");
        assertThatThrownBy(() -> a.estSain(new byte[]{1})).isInstanceOf(Antivirus.AntivirusIndisponible.class)
                .hasMessageContaining("ERROR");
    }

    @Test
    void clamd_injoignable_rendLAnalyseImpossible() throws IOException {
        int port;
        try (ServerSocket libre = new ServerSocket(0)) {
            port = libre.getLocalPort();
        }
        var a = new AntivirusClamd("127.0.0.1", port, 500);
        assertThatThrownBy(() -> a.estSain(new byte[]{1})).isInstanceOf(Antivirus.AntivirusIndisponible.class);
    }

    @Test
    void clamd_sansHote_echoueAuDemarrage() {
        assertThatThrownBy(() -> new AntivirusClamd(" ", 3310, 1000)).hasMessageContaining("ANTIVIRUS_CLAMAV_HOTE");
        assertThatThrownBy(() -> new AntivirusClamd(null, 3310, 1000)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void signatures_reconnaissentEicar_seulement() {
        var s = new AntivirusSignatures();
        assertThat(s.estSain(EICAR)).isFalse();
        assertThat(s.estSain("EICAR-STANDARD".getBytes(StandardCharsets.US_ASCII))).isTrue();
        assertThat(s.estSain(new byte[0])).isTrue();
        assertThat(s.estSain("xxEICAR-STANDARD-ANTIVIRUS-TEST-FILx".getBytes(StandardCharsets.US_ASCII))).isTrue();
    }

    @Test
    void fichierInfecte_nommeLeFichier() {
        assertThat(new Antivirus.FichierInfecte("a.pdf")).hasMessageContaining("« a.pdf »").isInstanceOf(IllegalArgumentException.class);
    }
}
