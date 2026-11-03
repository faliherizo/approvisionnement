package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.PorteeTenant;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Parcours;

/** Contexte partagé des cas d'utilisation CLM : tenant courant, acteur, horloge et sérialisation JSON. */
@Component
public class ContexteClm {

    private static final TypeReference<List<Parcours.Affectation>> AFFECTATIONS = new TypeReference<>() {
    };

    private final PerimetreUtilisateur perimetre;
    private final PorteeTenant portee;
    private final ObjectMapper json;
    private final Clock horloge;

    public ContexteClm(PerimetreUtilisateur perimetre, PorteeTenant portee, ObjectMapper json, Clock horloge) {
        this.perimetre = perimetre;
        this.portee = portee;
        this.json = json;
        this.horloge = horloge;
    }

    public Long tenant() {
        Long t = portee.courant();
        if (t == null) {
            throw new IllegalStateException("Opération réservée à un espace tenant.");
        }
        return t;
    }

    /** Traitement hors requête (portail, planificateur) rattaché au tenant de la donnée traitée. */
    public <T> T dansTenant(Long tenantId, Supplier<T> action) {
        return portee.executerDans(tenantId, action);
    }

    public Habilitations.Acteur acteur(String id, Collection<String> roles) {
        return new Habilitations.Acteur(id, java.util.Set.copyOf(roles), perimetre.groupes(tenant(), id));
    }

    /** Millisecondes seulement : l'horodatage relu de la base doit redonner exactement l'empreinte scellée. */
    public Instant maintenant() {
        return Instant.now(horloge).truncatedTo(ChronoUnit.MILLIS);
    }

    public String ecrire(Object valeur) {
        try {
            return json.writeValueAsString(valeur);
        } catch (Exception e) {
            throw new IllegalStateException("Sérialisation impossible", e);
        }
    }

    /** JSON indenté, pour les fichiers destinés à être relus hors de l'application (dossier de preuve). */
    public byte[] ecrireLisible(Object valeur) {
        try {
            return json.writerWithDefaultPrettyPrinter().writeValueAsBytes(valeur);
        } catch (Exception e) {
            throw new IllegalStateException("Sérialisation impossible", e);
        }
    }

    public <T> T lire(String texte, TypeReference<T> type, T defaut) {
        if (texte == null || texte.isBlank()) {
            return defaut;
        }
        try {
            return json.readValue(texte, type);
        } catch (Exception e) {
            return defaut;
        }
    }

    public Map<String, Object> lireObjet(String texte) {
        return lire(texte, new TypeReference<Map<String, Object>>() {
        }, Map.of());
    }

    public List<Parcours.Affectation> affectations(String texte) {
        return lire(texte, AFFECTATIONS, List.of());
    }
}
