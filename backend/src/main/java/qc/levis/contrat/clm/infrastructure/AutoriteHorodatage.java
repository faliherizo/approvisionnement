package qc.levis.contrat.clm.infrastructure;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/** Client RFC 3161 de l'autorité d'horodatage retenue : jeton sur une empreinte SHA-256, réponse vérifiée. */
final class AutoriteHorodatage {

    private final URI adresse;
    private final Clock horloge;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    AutoriteHorodatage(String adresse, Clock horloge) {
        this.adresse = URI.create(adresse);
        this.horloge = horloge;
    }

    TimeStampToken jeton(byte[] empreinteSha256) {
        TimeStampRequestGenerator g = new TimeStampRequestGenerator();
        g.setCertReq(true);
        TimeStampRequest requete = g.generate(NISTObjectIdentifiers.id_sha256, empreinteSha256, BigInteger.valueOf(horloge.millis()));
        try {
            HttpResponse<byte[]> r = http.send(HttpRequest.newBuilder(adresse).timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/timestamp-query")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requete.getEncoded())).build(), HttpResponse.BodyHandlers.ofByteArray());
            if (r.statusCode() != 200) {
                throw new IllegalStateException("Autorité d'horodatage indisponible : réponse HTTP " + r.statusCode() + ".");
            }
            TimeStampResponse reponse = new TimeStampResponse(r.body());
            reponse.validate(requete);
            if (reponse.getTimeStampToken() == null) {
                throw new IllegalStateException("Horodatage refusé par l'autorité : " + reponse.getStatusString());
            }
            return reponse.getTimeStampToken();
        } catch (TSPException e) {
            throw new IllegalStateException("Réponse d'horodatage invalide : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Autorité d'horodatage injoignable : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Horodatage interrompu.", e);
        }
    }
}
