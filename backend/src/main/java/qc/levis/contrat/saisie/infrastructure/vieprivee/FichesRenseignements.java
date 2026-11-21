package qc.levis.contrat.saisie.infrastructure.vieprivee;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/**
 * Fiches remplies par la personne. Un brouillon n'est qu'à elle : il est supprimé. Une fiche soumise est un document de
 * l'organisation, soumis à son calendrier de conservation : il est conservé, et la personne n'en est plus l'auteur nommé.
 */
@Component
class FichesRenseignements implements SourceRenseignementsPersonnels {

    static final String MOTIF_CONSERVATION = "Fiches soumises : documents de l'organisation, conservés selon son calendrier de "
            + "conservation (Loi sur les archives) ; l'auteur y est remplacé par un pseudonyme.";

    private final SaisieRepository fiches;

    FichesRenseignements(SaisieRepository fiches) { this.fiches = fiches; }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        List<Renseignement> elements = fiches.findByTenantIdAndCreeParIgnoreCase(tenantId, courriel).stream()
                .map(f -> Renseignement.de("fiche/" + f.getId(), f.getMajLe(), "numero", f.getNumero(),
                        "statut", f.getStatut().name(), "donnees", f.getDonneesJson()))
                .toList();
        return new ExtraitRenseignements("Fiches et demandes", "Données saisies dans les formulaires et suivi des demandes.",
                "Les brouillons sont supprimés ; les fiches soumises sont conservées sous pseudonyme.", elements);
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        List<Saisie> liste = fiches.findByTenantIdAndCreeParIgnoreCase(tenantId, courriel);
        List<Saisie> brouillons = liste.stream().filter(f -> f.getStatut() == StatutSaisie.BROUILLON).toList();
        List<Saisie> soumises = liste.stream().filter(f -> f.getStatut() != StatutSaisie.BROUILLON).toList();
        fiches.deleteAll(brouillons);
        soumises.forEach(f -> f.setCreePar(pseudonyme.courriel()));
        fiches.saveAll(soumises);
        return new BilanEffacement("Fiches et demandes", brouillons.size(), soumises.size(),
                soumises.isEmpty() ? null : MOTIF_CONSERVATION);
    }
}
