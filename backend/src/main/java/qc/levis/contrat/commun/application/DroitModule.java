package qc.levis.contrat.commun.application;

import java.util.Objects;

/**
 * Droit accordé par un module à un compte, exposé en lecture (RST-52) : par exemple l'appartenance au groupe « JURIDIQUE »
 * du module CLM. La valeur publiée, « module:code », reste unique d'un module à l'autre.
 *
 * @param module  code du module qui accorde le droit (« clm »)
 * @param code    code stable du droit dans le module
 * @param libelle libellé lisible ; le code à défaut
 */
public record DroitModule(String module, String code, String libelle) {

    public DroitModule {
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(code, "code");
        libelle = libelle == null || libelle.isBlank() ? code : libelle;
    }

    /** Valeur SCIM de l'entitlement : « module:code ». */
    public String valeur() {
        return module + ":" + code;
    }
}
