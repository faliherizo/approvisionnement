package qc.levis.contrat.commun.module.infrastructure.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Module dont relève un contrôleur : si aucun des codes n'est actif pour le tenant courant, {@link GardeModules} refuse la
 * requête (403) avant toute exécution. Plusieurs codes se lisent « l'un ou l'autre » : une donnée partagée par deux modules
 * reste accessible dès que l'un des deux est souscrit.
 * <p>
 * Pensée pour les contrôleurs dont les méthodes portent leurs propres {@code @PreAuthorize} de rôle : une annotation de
 * méthode remplace celle de la classe, alors que cette garde-ci s'applique toujours.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModuleRequis {
    String[] value();
}
