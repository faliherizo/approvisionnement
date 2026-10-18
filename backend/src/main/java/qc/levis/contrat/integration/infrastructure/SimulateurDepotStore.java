package qc.levis.contrat.integration.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Magasin des dépôts <em>simulés</em> : écrit réellement sur disque, dans une arborescence par
 * environnement (SharePoint/OneDrive/Teams/Outlook/SAP) et par tenant, afin de pouvoir visualiser et
 * télécharger les fichiers effectivement déposés lorsque l'intégration réelle n'est pas branchée.
 *
 * <p>Arborescence : {@code {base}/{tenantId}/{cible}/{destination}/{nomFichier}}. Isolation par
 * tenant, protection contre la traversée de répertoire à l'écriture comme à la lecture.
 */
@Component
public class SimulateurDepotStore {

    private final Path base;

    public SimulateurDepotStore(
            @Value("${app.publication.simulateur.repertoire:data/simulateur}") String repertoire) {
        this.base = Path.of(repertoire).toAbsolutePath().normalize();
    }

    /** Écrit le contenu déposé et renvoie son chemin relatif (sous le tenant). */
    public String enregistrer(String cible, Long tenantId, String destination,
                              String nomFichier, byte[] octets) {
        Path racineTenant = racineTenant(tenantId);
        Path cible0 = racineTenant.resolve(segment(cible));
        Path dossier = destination == null || destination.isBlank()
                ? cible0 : cible0.resolve(sousChemin(destination));
        // Tous les segments sont déjà assainis (pas de séparateur ni de « .. ») : le fichier reste
        // sous la racine du tenant par construction.
        Path fichier = dossier.resolve(segment(nomFichier)).normalize();
        try {
            Files.createDirectories(fichier.getParent());
            Files.write(fichier, octets);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture du dépôt simulé impossible", e);
        }
        return racineTenant.relativize(fichier).toString().replace('\\', '/');
    }

    /** Liste tous les dépôts d'un tenant (triés du plus récent au plus ancien). */
    public List<Depot> lister(Long tenantId) {
        Path racine = racineTenant(tenantId);
        if (!Files.isDirectory(racine)) return List.of();
        List<Depot> depots = new ArrayList<>();
        try (var flux = Files.walk(racine)) {
            flux.filter(Files::isRegularFile).forEach(p -> depots.add(versDepot(racine, p)));
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture des dépôts simulés impossible", e);
        }
        depots.sort(Comparator.comparing(Depot::depuis).reversed());
        return depots;
    }

    /** Lit le contenu d'un dépôt (chemin relatif au tenant), avec garde anti-traversée. */
    public byte[] lire(Long tenantId, String cheminRelatif) {
        Path racine = racineTenant(tenantId);
        Path fichier = racine.resolve(cheminRelatif).normalize();
        if (!fichier.startsWith(racine) || !Files.isRegularFile(fichier)) {
            throw new IllegalArgumentException("Dépôt introuvable.");
        }
        try {
            return Files.readAllBytes(fichier);
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du dépôt impossible", e);
        }
    }

    private Depot versDepot(Path racineTenant, Path fichier) {
        String rel = racineTenant.relativize(fichier).toString().replace('\\', '/');
        String cible = rel.contains("/") ? rel.substring(0, rel.indexOf('/')) : rel;
        try {
            BasicFileAttributes a = Files.readAttributes(fichier, BasicFileAttributes.class);
            return new Depot(cible, rel, fichier.getFileName().toString(), a.size(),
                    a.lastModifiedTime().toInstant());
        } catch (IOException e) {
            throw new UncheckedIOException("Attributs du dépôt illisibles", e);
        }
    }

    private Path racineTenant(Long tenantId) {
        return base.resolve(tenantId == null ? "global" : String.valueOf(tenantId));
    }

    /** Un seul segment de chemin, débarrassé de tout séparateur (anti-traversée). */
    private String segment(String v) {
        String s = v == null ? "" : v.replace('\\', '/');
        s = s.contains("/") ? s.substring(s.lastIndexOf('/') + 1) : s;
        return s.isBlank() ? "sans-nom" : s;
    }

    /** Sous-chemin multi-segments nettoyé (retire les « .. » et segments vides). */
    private String sousChemin(String destination) {
        String[] segments = destination.replace('\\', '/').split("/");
        StringBuilder sb = new StringBuilder();
        for (String seg : segments) {
            if (seg.isBlank() || seg.equals("..") || seg.equals(".")) continue;
            if (sb.length() > 0) sb.append('/');
            sb.append(seg);
        }
        return sb.toString();
    }

    /** Métadonnées d'un dépôt simulé. */
    public record Depot(String cible, String chemin, String nomFichier, long taille, Instant depuis) {}
}
