package qc.levis.contrat.vitrine.application.port;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Médiathèque de la plateforme : vidéos de présentation et tutoriels, sous-titres, affiches et index de l'aide. Les
 * fichiers vivent hors de la base (un répertoire monté) : une vidéo se lit par plages, sans tout charger en mémoire.
 */
public interface MediaStore {

    /** Un fichier de la médiathèque : chemin relatif, taille en octets. */
    record Media(String chemin, long taille) {
    }

    /** Fichier existant à ce chemin relatif ; vide s'il n'existe pas ou si le chemin sort de la médiathèque. */
    Optional<Path> fichier(String chemin);

    /** Fichiers sous un préfixe (ex. « vitrines/ »), triés par chemin. */
    List<Media> lister(String prefixe);

    /** Écrit (ou remplace) un fichier ; les répertoires intermédiaires sont créés. */
    void enregistrer(String chemin, byte[] contenu);
}
