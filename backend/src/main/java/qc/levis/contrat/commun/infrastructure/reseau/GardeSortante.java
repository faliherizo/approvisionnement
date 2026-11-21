package qc.levis.contrat.commun.infrastructure.reseau;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Garde des requêtes sortantes vers une adresse fournie par une organisation (OWASP A10, SSRF) : émetteur OIDC, SAP,
 * Salesforce, rappels. Seuls HTTP et HTTPS sont admis, et jamais vers une adresse interne (boucle locale, réseau privé,
 * lien local dont les métadonnées infonuagiques 169.254.169.254, multidiffusion), sauf hôte:port explicitement autorisé
 * par la configuration (bacs à sable locaux en développement ; liste vide en production).
 */
@Component
public class GardeSortante {

    /** Résolution DNS injectée : les tests n'interrogent pas le réseau. */
    @FunctionalInterface
    public interface Resolveur {
        InetAddress[] resoudre(String hote) throws UnknownHostException;
    }

    private final Set<String> autorises;
    private final Resolveur resolveur;

    @org.springframework.beans.factory.annotation.Autowired
    public GardeSortante(@Value("${app.securite.sortant.autorises:}") String autorises) {
        this(Arrays.stream(autorises.split(",")).map(String::strip).filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet()), InetAddress::getAllByName);
    }

    public GardeSortante(Set<String> autorises, Resolveur resolveur) {
        this.autorises = Set.copyOf(autorises);
        this.resolveur = resolveur;
    }

    /** Garde inactive, pour les essais unitaires d'adaptateurs contre un serveur local. */
    public static GardeSortante aucune() {
        return new GardeSortante(Set.of("*"), h -> new InetAddress[0]);
    }

    public URI exiger(String url) {
        URI uri;
        try {
            uri = URI.create(url == null ? "" : url.strip());
        } catch (IllegalArgumentException e) {
            throw new DestinationRefusee("Adresse refusée : URL invalide.");
        }
        exiger(uri);
        return uri;
    }

    public void exiger(URI uri) {
        if (autorises.contains("*")) return;
        String schema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!schema.equals("https") && !schema.equals("http")) throw new DestinationRefusee("Adresse refusée : seul HTTP(S) est admis.");
        String hote = uri.getHost();
        if (hote == null || hote.isBlank()) throw new DestinationRefusee("Adresse refusée : hôte absent.");
        int port = uri.getPort() != -1 ? uri.getPort() : (schema.equals("https") ? 443 : 80);
        if (autorises.contains((hote + ":" + port).toLowerCase(Locale.ROOT))) return;
        for (InetAddress a : resoudre(hote)) {
            if (estInterne(a)) throw new DestinationRefusee("Adresse refusée : « " + hote + " » désigne une adresse interne.");
        }
    }

    private InetAddress[] resoudre(String hote) {
        try {
            return resolveur.resoudre(hote.startsWith("[") ? hote.substring(1, hote.length() - 1) : hote);
        } catch (UnknownHostException e) {
            throw new DestinationRefusee("Adresse refusée : hôte inconnu.");
        }
    }

    static boolean estInterne(InetAddress a) {
        if (a.isLoopbackAddress() || a.isLinkLocalAddress() || a.isSiteLocalAddress() || a.isAnyLocalAddress() || a.isMulticastAddress()) {
            return true;
        }
        byte[] o = a.getAddress();
        boolean ula = a instanceof Inet6Address && (o[0] & 0xFE) == 0xFC;
        boolean cgnat = o.length == 4 && (o[0] & 0xFF) == 100 && (o[1] & 0xC0) == 64;
        return ula || cgnat;
    }

    /** La destination n'est pas admise. */
    public static class DestinationRefusee extends IllegalArgumentException {
        public DestinationRefusee(String message) {
            super(message);
        }
    }
}
