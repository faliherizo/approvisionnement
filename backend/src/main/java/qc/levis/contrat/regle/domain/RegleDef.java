package qc.levis.contrat.regle.domain;

import java.util.List;

/**
 * Règle d'affaires configurable (stockée dans la structure JSON du formulaire, jamais en dur).
 * {@code type} : VISIBILITE | OBLIGATION | VALIDATION | CALCUL | ROUTAGE.
 * {@code combinateur} : ET | OU (par défaut ET). {@code cible} : champ/rôle visé.
 * {@code valeur} : message (VALIDATION), expression (CALCUL : « somme:champ1,champ2 »), rôle (ROUTAGE).
 */
public record RegleDef(String type, String combinateur, List<ConditionDef> conditions,
                       String cible, String valeur) {}
