package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import qc.levis.contrat.securite.domain.NumeroCarte;

/** IQ-D-14 (ADR-022) : un numéro de carte se reconnaît au préfixe, à la longueur et à la clé de Luhn. */
class NumeroCarteTest {

    @ParameterizedTest(name = "{1} : {0}")
    @CsvSource({
            "4111111111111111, VISA",
            "4222222222222, VISA",
            "4111 1111 1111 1111, VISA",
            "5555-5555-5555-4444, MASTERCARD",
            "2223003122003222, MASTERCARD",
            "378282246310005, AMEX",
            "6011111111111117, DISCOVER",
            "6445644564456445, DISCOVER",
            "3530111333300000, JCB",
            "30569309025904, DINERS",
            "36227206271667, DINERS",
            "6200000000000005, UNIONPAY"})
    void les_numeros_de_test_des_reseaux_sont_reconnus(String numero, NumeroCarte.Reseau reseau) {
        assertEquals(reseau, NumeroCarte.detecter("Payé avec " + numero + ", merci.").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4111111111111112",          // clé de Luhn fausse
            "1234567812345670",          // Luhn valide, aucun réseau
            "41111111111111111111",      // trop long, collé
            "x4111111111111111111111",   // suite de chiffres plus longue qu'un PAN
            "411111111111111",           // Visa à 15 chiffres
            "5555555555554",             // Mastercard à 13 chiffres
            "1732200000000",             // horodatage en millisecondes
            "+1 514 555 0101",           // téléphone
            "2026-11-21",                // date
            "4111--1111-1111-1111",      // séparateurs doublés
            "-4111111111111111"})        // collé à un tiret (identifiant composé)
    void les_suites_qui_ne_sont_pas_des_cartes_passent(String texte) {
        assertTrue(NumeroCarte.detecter(texte).isEmpty(), texte);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "court"})
    void un_texte_vide_ou_court_passe(String texte) {
        assertTrue(NumeroCarte.detecter(texte).isEmpty());
        assertTrue(NumeroCarte.detecter(null).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"note\":\"9999999999999995 puis 4012888888881881\"}"})
    void le_premier_vrai_numero_est_trouve_apres_un_faux_candidat(String texte) {
        assertEquals(NumeroCarte.Reseau.VISA, NumeroCarte.detecter(texte).orElseThrow());
    }
}
