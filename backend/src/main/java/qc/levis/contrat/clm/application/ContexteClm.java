package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Contexte partagé des cas d'utilisation CLM : tenant courant, acteur, horloge et sérialisation JSON. */
@Component
public class ContexteClm {

    private static final TypeReference<List<Parcours.Affectation>> AFFECTATIONS = new TypeReference<>() {
    };

    private final PerimetreUtilisateur perimetre;
    private final ObjectMapper json;
    private final Clock horloge;

    public ContexteClm(PerimetreUtilisateur perimetre, ObjectMapper json, Clock horloge) {
        this.perimetre = perimetre;
        this.json = json;
        this.horloge = horloge;
    }

    public Long tenant() {
        Long t = TenantContext.get();
        if (t == null) {
            throw new IllegalStateException("Opération réservée à un espace tenant.");
        }
        return t;
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

    public ObjectMapper json() {
        return json;
    }
}
