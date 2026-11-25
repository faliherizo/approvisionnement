package qc.levis.contrat.vitrine.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vitrine.application.port.MediaStore;

/**
 * Médiathèque sur disque (APP_MEDIA_REPERTOIRE, volume monté en production). Tout chemin est résolu sous la racine et
 * refusé s'il en sort : la médiathèque est servie publiquement, elle ne doit rien exposer d'autre.
 */
@Component
public class MediaFichiers implements MediaStore {

    private final Path racine;

    public MediaFichiers(@Value("${app.media.repertoire:./data/media}") String repertoire) {
        this.racine = Path.of(repertoire).toAbsolutePath().normalize();
    }

    @Override
    public Optional<Path> fichier(String chemin) {
        return sous(chemin).filter(Files::isRegularFile);
    }

    @Override
    public List<Media> lister(String prefixe) {
        if (!Files.isDirectory(racine)) {
            return List.of();
        }
        try (Stream<Path> tous = Files.walk(racine)) {
            return tous.filter(Files::isRegularFile).map(p -> new Media(racine.relativize(p).toString().replace('\\', '/'), taille(p)))
                    .filter(m -> m.chemin().startsWith(prefixe)).sorted(java.util.Comparator.comparing(Media::chemin)).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Médiathèque illisible", e);
        }
    }

    @Override
    public void enregistrer(String chemin, byte[] contenu) {
        Path cible = sous(chemin).orElseThrow(() -> new IllegalArgumentException("Chemin hors de la médiathèque : " + chemin));
        try {
            Files.createDirectories(cible.getParent());
            Files.write(cible, contenu);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture impossible dans la médiathèque : " + chemin, e);
        }
    }

    private Optional<Path> sous(String chemin) {
        if (chemin == null || chemin.isBlank()) {
            return Optional.empty();
        }
        Path p = racine.resolve(chemin).normalize();
        return p.startsWith(racine) && !p.equals(racine) ? Optional.of(p) : Optional.empty();
    }

    private static long taille(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            return 0;
        }
    }
}
