package qc.levis.contrat.securite.domain;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Adresse du client à qui imputer une requête, pour la limitation de débit (constat SEC-33). L'en-tête X-Forwarded-For
 * est fourni par le client : seule l'entrée qu'y AJOUTE un mandataire de confiance (frontal nginx, répartiteur, tous sur
 * un réseau privé) est fiable. On lit donc l'en-tête de droite à gauche, tant que l'adresse est celle d'un mandataire, et
 * l'on retient la première adresse publique. Une requête reçue directement d'une adresse publique ignore l'en-tête.
 */
public final class AdresseClient {

    /** IPv4 pointée ou IPv6 (au moins un deux-points) : jamais un nom, qui déclencherait une résolution DNS. */
    private static final Pattern LITTERAL = Pattern.compile("^(\\d{1,3}(\\.\\d{1,3}){3}|[0-9a-fA-F.]*:[0-9a-fA-F:.]*)$");

    private AdresseClient() {}

    public static String resoudre(String adresseDistante, String transmisePour) {
        if (!estMandataire(adresseDistante) || transmisePour == null || transmisePour.isBlank()) return adresseDistante;
        String[] chaine = transmisePour.split(",");
        String retenue = adresseDistante;
        for (int i = chaine.length - 1; i >= 0; i--) {
            String a = chaine[i].strip();
            if (litteral(a) == null) return retenue;
            retenue = a;
            if (!estMandataire(a)) return a;
        }
        return retenue;
    }

    /** Boucle locale, réseau privé, lien local ou ULA : adresse d'un mandataire de l'hébergement, jamais d'un usager. */
    static boolean estMandataire(String adresse) {
        InetAddress a = litteral(adresse);
        if (a == null) return false;
        boolean ula = a instanceof Inet6Address && (a.getAddress()[0] & 0xFE) == 0xFC;
        return a.isLoopbackAddress() || a.isSiteLocalAddress() || a.isLinkLocalAddress() || ula;
    }

    /** Adresse littérale, sans aucune résolution DNS ; {@code null} si ce n'est pas une adresse valide. */
    static InetAddress litteral(String adresse) {
        if (adresse == null || !LITTERAL.matcher(adresse).matches()) return null;
        try {
            if (adresse.indexOf(':') >= 0) return InetAddress.getByName(adresse); // IPv6 : analysée comme littéral, jamais résolue
            String[] octets = adresse.split("\\.");
            byte[] brut = new byte[4];
            for (int i = 0; i < 4; i++) {
                int o = Integer.parseInt(octets[i]);
                if (o > 255) return null;
                brut[i] = (byte) o;
            }
            return InetAddress.getByAddress(brut);
        } catch (UnknownHostException malforme) {
            return null;
        }
    }
}
