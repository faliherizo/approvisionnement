package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Bornes des paramètres et de la connexion Salesforce qu'une organisation configure (UI-21, UI-22). */
class ConfigurationDomaineClmTest {

    private static final Instant T0 = Instant.parse("2026-11-02T10:00:00Z");
    private static final long PLAFOND = 30L * ParametresClm.UN_MO;
    private static final List<String> HOTES = List.of("salesforce.com", " .force.com ", " ");

    private static ParametresClm.Reglages reglages(long taille, int validite, String portail, String prefixe, int frequence, int rappels,
                                                   Identite.Politique identite, String champ) {
        return new ParametresClm.Reglages(taille, validite, portail, prefixe, frequence, rappels, Relances.PREAVIS_PAR_DEFAUT_JOURS,
                identite, champ);
    }

    private static ParametresClm.Reglages valides() {
        return reglages(ParametresClm.UN_MO, 14, "https://iq.example/portail", "IQ", 2, 3, Identite.Politique.DEFAUT, "montantAide");
    }

    @Test
    void les_parametres_d_une_organisation_se_conservent_tels_quels() {
        ParametresClm p = new ParametresClm(7L);
        Map<Identite.Risque, Identite.Niveau> stricte = Map.of(Identite.Risque.FAIBLE, Identite.Niveau.CODE_SMS,
                Identite.Risque.MOYEN, Identite.Niveau.QUESTION, Identite.Risque.ELEVE, Identite.Niveau.FORTE);
        ParametresClm.Reglages r = reglages(2 * ParametresClm.UN_MO, 7, "https://iq.example/portail", "IQ", 5, 2,
                new Identite.Politique(stricte, Identite.Politique.DEFAUT.internes(), 3, 15), "montantAide");
        p.appliquer(r, "admin@iq.example", T0);
        assertEquals(r, p.reglages());
        assertEquals(7L, p.getTenantId());
        assertEquals("admin@iq.example", p.getModifiePar());
        assertEquals(T0, p.getModifieLe());
        assertTrue(valides().erreurs(PLAFOND).isEmpty());
        assertTrue(reglages(ParametresClm.TAILLE_MIN_OCTETS, 1, "http://localhost:5173/portail", "A", 1, 0, Identite.Politique.DEFAUT, "m")
                .erreurs(PLAFOND).isEmpty(), "bornes basses admises");
        assertTrue(reglages(PLAFOND, ParametresClm.VALIDITE_MAX_JOURS, "https://x.ca", "ABCDEFGHI9", Relances.FREQUENCE_MAX_JOURS,
                Relances.RAPPELS_PLAFOND, Identite.Politique.DEFAUT, "m").erreurs(PLAFOND).isEmpty(), "bornes hautes admises");
    }

    @Test
    void chaque_parametre_hors_bornes_est_nomme() {
        List<String> trop = reglages(PLAFOND + 1, ParametresClm.VALIDITE_MAX_JOURS + 1, "ftp://iq.example", "TROPLONGPREFIXE",
                Relances.FREQUENCE_MAX_JOURS + 1, Relances.RAPPELS_PLAFOND + 1, null, "1champ").erreurs(PLAFOND);
        assertEquals(8, trop.size(), trop.toString());
        assertTrue(trop.get(0).contains("30 Mo"));
        List<String> peu = reglages(ParametresClm.TAILLE_MIN_OCTETS - 1, 0, null, null, 0, -1,
                new Identite.Politique(Map.of(), Map.of(), 5, 10), null).erreurs(PLAFOND);
        assertEquals(9, peu.size(), peu.toString());
    }

    @Test
    void le_preavis_d_echeance_va_de_zero_aucune_alerte_a_trente_jours() {
        ParametresClm.Reglages v = valides();
        for (int admis : new int[]{0, Relances.PREAVIS_MAX_JOURS}) {
            assertTrue(new ParametresClm.Reglages(v.tailleMaxOctets(), v.invitationValiditeJours(), v.portailUrl(), v.prefixeReference(),
                    v.rappelFrequenceJours(), v.rappelsMax(), admis, v.identite(), v.champMontant()).erreurs(PLAFOND).isEmpty());
        }
        for (int refuse : new int[]{-1, Relances.PREAVIS_MAX_JOURS + 1}) {
            assertEquals(List.of("Préavis d'échéance : entre 0 (aucune alerte) et 30 jours."),
                    new ParametresClm.Reglages(v.tailleMaxOctets(), v.invitationValiditeJours(), v.portailUrl(), v.prefixeReference(),
                            v.rappelFrequenceJours(), v.rappelsMax(), refuse, v.identite(), v.champMontant()).erreurs(PLAFOND));
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "iq.example/portail", "https://", "https:/chemin", "https://iq example/p", "mailto:a@b.ca"})
    void une_adresse_de_portail_doit_etre_une_url_web_complete(String url) {
        assertFalse(ParametresClm.Reglages.adresseWeb(url));
    }

    @Test
    void une_connexion_propre_conserve_son_secret_tant_qu_aucun_autre_n_est_fourni() {
        ConnexionSalesforce c = new ConnexionSalesforce(7L);
        assertFalse(c.secretConfigure());
        ConnexionSalesforce.Reglages r = new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST, "https://iq.my.salesforce.com",
                "https://login.salesforce.com/services/oauth2/token", "client", "FundingRequest", "v61.0",
                new ConnexionSalesforce.ChampsRetour("Etat__c", "Reference__c", "Trace__c"), 4);
        c.appliquer(r, "chiffre-1", "admin@iq.example", T0);
        assertEquals(r, c.reglages());
        assertTrue(c.secretConfigure());
        c.appliquer(r, null, "admin@iq.example", T0);
        assertEquals("chiffre-1", c.getSecretChiffre(), "un secret absent conserve celui en place");
        c.appliquer(r, " ", "autre@iq.example", T0.plusSeconds(1));
        assertFalse(c.secretConfigure(), "un secret blanc n'en est pas un");
        assertEquals(7L, c.getTenantId());
        assertEquals("autre@iq.example", c.getModifiePar());
        assertEquals(T0.plusSeconds(1), c.getModifieLe());
        assertTrue(r.erreurs(HOTES, true).isEmpty());
    }

    @Test
    void la_simulation_n_exige_ni_url_ni_identifiants_mais_des_noms_d_api_valides() {
        ConnexionSalesforce.ChampsRetour champs = new ConnexionSalesforce.ChampsRetour("CLM_Statut__c", "CLM_Reference__c", "CLM_Correlation__c");
        assertTrue(new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.SIMULATION, null, null, null, "FundingRequest", "v60.0",
                champs, 1).erreurs(HOTES, false).isEmpty());
        assertEquals(List.of("Mode d'intégration requis : SIMULATION ou REST."),
                new ConnexionSalesforce.Reglages(null, null, null, null, null, null, null, 0).erreurs(HOTES, false));
        List<String> erreurs = new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.SIMULATION, null, null, null, "Funding Request",
                null, null, 0).erreurs(HOTES, false);
        assertEquals(4, erreurs.size(), erreurs.toString());
        List<String> champsFautifs = new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.SIMULATION, null, null, null, null, "60",
                new ConnexionSalesforce.ChampsRetour(null, "1x", "a-b"), ConnexionSalesforce.REPRISES_PLAFOND + 1).erreurs(HOTES, false);
        assertEquals(6, champsFautifs.size(), champsFautifs.toString());
    }

    @Test
    void le_jwt_bearer_exige_un_utilisateur_d_execution_et_une_cle_privee() {
        ConnexionSalesforce.ChampsRetour champs = new ConnexionSalesforce.ChampsRetour("A", "B", "C");
        java.util.function.BiFunction<String, Boolean, List<String>> erreurs = (utilisateur, cle) -> new ConnexionSalesforce.Reglages(
                ConnexionSalesforce.Mode.REST, "https://iq.my.salesforce.com", "https://login.salesforce.com/services/oauth2/token", "client",
                "FundingRequest", "v60.0", champs, 8, ConnexionSalesforce.Authentification.JWT_BEARER, utilisateur).erreurs(HOTES, cle);
        assertTrue(erreurs.apply("integration@iq.example", true).isEmpty());
        assertEquals(List.of("Clé privée requise en JWT Bearer.", "Utilisateur d'exécution requis en JWT Bearer (255 caractères au plus)."),
                erreurs.apply(null, false));
        assertEquals(1, erreurs.apply(" ", true).size());
        assertEquals(1, erreurs.apply("u".repeat(256), true).size());
        assertEquals(ConnexionSalesforce.Authentification.CLIENT_CREDENTIALS, new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST,
                null, null, null, "FundingRequest", "v60.0", champs, 8, null, null).authentification(), "méthode par défaut");

        ConnexionSalesforce c = new ConnexionSalesforce(9L);
        ConnexionSalesforce.Reglages jwt = new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST, "https://iq.my.salesforce.com",
                "https://login.salesforce.com/services/oauth2/token", "client", "FundingRequest", "v60.0", champs, 8,
                ConnexionSalesforce.Authentification.JWT_BEARER, "integration@iq.example");
        c.appliquer(jwt, "cle-chiffree", "admin@iq.example", T0);
        assertEquals(jwt, c.reglages(), "méthode et utilisateur conservés");
    }

    @Test
    void le_mode_rest_exige_des_hotes_autorises_un_client_et_un_secret() {
        ConnexionSalesforce.ChampsRetour champs = new ConnexionSalesforce.ChampsRetour("A", "B", "C");
        List<String> erreurs = new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST, "http://iq.my.salesforce.com", null, " ",
                "FundingRequest", "v61.0", champs, 8).erreurs(HOTES, false);
        assertEquals(4, erreurs.size(), erreurs.toString());
        assertTrue(erreurs.get(0).contains("(salesforce.com, force.com)"), erreurs.get(0));
        assertEquals(1, new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST, "https://iq.lightning.force.com",
                "https://force.com/token", "x".repeat(256), "FundingRequest", "v61.0", champs, 8).erreurs(HOTES, true).size());
        assertEquals(1, new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.REST, "https://iq.lightning.force.com",
                "https://force.com/token", null, "FundingRequest", "v61.0", champs, 8).erreurs(HOTES, true).size());
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NUL", value = {
            "NUL,false", "' ',false", "https://salesforce.com,true", "https://iq.my.salesforce.com/x,true", "https://evilsalesforce.com,false",
            "https://salesforce.com.evil.example,false", "https://user@iq.my.salesforce.com,false", "https:///sans-hote,false",
            "http://iq.my.salesforce.com,false", "'https://iq salesforce.com',false", "https://IQ.Force.COM,true"})
    void seuls_les_domaines_autorises_sont_joignables(String url, boolean attendu) {
        assertEquals(attendu, ConnexionSalesforce.Reglages.hoteAutorise(url, HOTES), String.valueOf(url));
    }

    @Test
    void sans_politique_d_identite_les_parametres_sont_refuses() {
        assertTrue(reglages(ParametresClm.UN_MO, 14, "https://x.ca", "IQ", 2, 3, null, "m").erreurs(PLAFOND)
                .contains("Politique d'identité requise."));
    }
}
