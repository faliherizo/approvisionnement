package qc.levis.contrat.dossier.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.dossier.application.repository.RepertoireDepotRepository;
import qc.levis.contrat.dossier.domain.RepertoireDepot;

/**
 * Répertoires de dépôt personnels d'un utilisateur (self-service). Chaque utilisateur ajoute ou
 * retire ses propres dossiers de destination ; le chemin est normalisé et protégé contre la
 * traversée de répertoire, comme à la publication.
 */
@Service
public class RepertoireDepotService {

    private final RepertoireDepotRepository repo;

    public RepertoireDepotService(RepertoireDepotRepository repo) {
        this.repo = repo;
    }

    public List<RepertoireDepot> lister(Long userId) {
        return repo.findByUserIdOrderByCheminAsc(userId);
    }

    @Transactional
    public RepertoireDepot ajouter(Long userId, String cheminBrut) {
        String chemin = normaliser(cheminBrut);
        if (chemin.isBlank()) {
            throw new IllegalArgumentException("Le répertoire ne peut pas être vide.");
        }
        if (repo.existsByUserIdAndChemin(userId, chemin)) {
            return repo.findByUserIdOrderByCheminAsc(userId).stream()
                    .filter(r -> r.getChemin().equals(chemin)).findFirst().orElseThrow();
        }
        return repo.save(new RepertoireDepot(userId, chemin));
    }

    @Transactional
    public void supprimer(Long userId, Long id) {
        RepertoireDepot r = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Répertoire introuvable."));
        repo.delete(r);
    }

    /** Normalise le chemin : retire les séparateurs superflus et refuse la traversée (« .. »). */
    String normaliser(String brut) {
        String base = brut == null ? "" : brut.trim().replace('\\', '/');
        int debut = 0;
        int fin = base.length();
        while (debut < fin && base.charAt(debut) == '/') debut++;
        while (fin > debut && base.charAt(fin - 1) == '/') fin--;
        String norme = base.substring(debut, fin);
        for (String segment : norme.split("/")) {
            if (segment.equals("..")) {
                throw new IllegalArgumentException("Chemin de répertoire invalide : " + brut);
            }
        }
        return norme;
    }
}
