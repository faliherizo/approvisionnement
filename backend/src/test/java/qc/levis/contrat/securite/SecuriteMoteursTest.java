package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.securite.application.LimiteurDebit;
import qc.levis.contrat.securite.application.Masquage;
import qc.levis.contrat.securite.application.MoteurAcces;
import qc.levis.contrat.securite.application.SujetsUtilisateur;
import qc.levis.contrat.securite.domain.RegleAcces;

/** Tests purs des moteurs de sécurité (accès granulaire, masquage, limiteur de débit). */
class SecuriteMoteursTest {

    @Test
    void acces_refus_par_defaut() {
        var u = new SujetsUtilisateur(Set.of("REDACTEUR"), Set.of(), "a@x.ca");
        assertFalse(MoteurAcces.autorise(List.of(), u, "CHAMP", "salaire", "LIRE"));
    }

    @Test
    void acces_accorde_par_role_groupe_utilisateur_et_joker() {
        var u = new SujetsUtilisateur(Set.of("APPRO"), Set.of("rh"), "a@x.ca");
        assertTrue(MoteurAcces.autorise(List.of(new RegleAcces("ROLE", "APPRO", "CHAMP", "salaire", "LIRE")), u, "CHAMP", "salaire", "LIRE"));
        assertTrue(MoteurAcces.autorise(List.of(new RegleAcces("GROUPE", "rh", "FICHE", "*", "MODIFIER")), u, "FICHE", "f1", "MODIFIER"));
        assertTrue(MoteurAcces.autorise(List.of(new RegleAcces("UTILISATEUR", "a@x.ca", "*", "*", "*")), u, "BOUTON", "exporter", "LIRE"));
        assertFalse(MoteurAcces.autorise(List.of(new RegleAcces("ROLE", "ADMIN", "CHAMP", "salaire", "LIRE")), u, "CHAMP", "salaire", "LIRE"));
    }

    @Test
    void masquage_par_classification() {
        assertTrue(Masquage.estMasque("SECRET", "INTERNE"));
        assertFalse(Masquage.estMasque("INTERNE", "CONFIDENTIEL"));
        assertFalse(Masquage.exportable("CONFIDENTIEL", "PUBLIC"));
        assertTrue(Masquage.exportable("PUBLIC", "PUBLIC"));
    }

    @Test
    void limiteur_de_debit_fenetre_glissante() {
        LimiteurDebit l = new LimiteurDebit();
        long t = 1_000_000L;
        assertTrue(l.autoriser("ip1", 2, 1000, t));
        assertTrue(l.autoriser("ip1", 2, 1000, t + 100));
        assertFalse(l.autoriser("ip1", 2, 1000, t + 200));   // 3e dans la fenêtre → refusé
        assertTrue(l.autoriser("ip1", 2, 1000, t + 1200));    // fenêtre écoulée → autorisé
    }
}
