package qc.levis.contrat.clm.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.repository.CompteurReferenceRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.CompteurReference;

/**
 * Numérotation des trousses (PREFIXE-AAAA-NNNNN) sûre sous concurrence. Le compteur de l'année est lu sous verrou dans la
 * transaction de création ; s'il n'existe pas encore, il est créé dans une transaction à part, en partant des références
 * déjà attribuées, et une création concurrente du même compteur est simplement ignorée.
 */
@Service
public class NumerotationReferences {

    private final CompteurReferenceRepository compteurs;
    private final TrousseRepository trousses;
    private final Initialisation initialisation;

    public NumerotationReferences(CompteurReferenceRepository compteurs, TrousseRepository trousses, Initialisation initialisation) {
        this.compteurs = compteurs;
        this.trousses = trousses;
        this.initialisation = initialisation;
    }

    /** À appeler dans la transaction qui enregistre la trousse : le verrou tient jusqu'à sa validation. */
    @Transactional(propagation = Propagation.MANDATORY)
    public String suivante(Long tenant, String prefixe, int annee) {
        CompteurReference c = compteurs.findByTenantIdAndPrefixeAndAnnee(tenant, prefixe, annee).orElseGet(() -> {
            try {
                initialisation.creer(tenant, prefixe, annee);
            } catch (DataIntegrityViolationException e) {
                // Créé au même instant par une autre requête : sa transaction à part est annulée, celui-là sert.
            }
            return compteurs.findByTenantIdAndPrefixeAndAnnee(tenant, prefixe, annee).orElseThrow();
        });
        String ref = c.suivante();
        // Une référence saisie à la main peut occuper un numéro : on passe au suivant.
        while (trousses.existsByTenantIdAndReference(tenant, ref)) {
            ref = c.suivante();
        }
        compteurs.save(c);
        return ref;
    }

    /** Création du compteur dans sa propre transaction, pour qu'il soit visible et verrouillable aussitôt. */
    @Service
    public static class Initialisation {

        private final CompteurReferenceRepository compteurs;
        private final TrousseRepository trousses;

        public Initialisation(CompteurReferenceRepository compteurs, TrousseRepository trousses) {
            this.compteurs = compteurs;
            this.trousses = trousses;
        }

        /** Lève {@link DataIntegrityViolationException} si le compteur vient d'être créé ailleurs : l'appelant l'ignore. */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void creer(Long tenant, String prefixe, int annee) {
            long deja = trousses.countByTenantIdAndReferenceStartingWith(tenant, prefixe + "-" + annee + "-");
            compteurs.saveAndFlush(new CompteurReference(tenant, prefixe, annee, deja));
        }
    }
}
