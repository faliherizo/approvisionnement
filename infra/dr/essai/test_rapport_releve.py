"""Tests de l'outil de mesure de l'essai de relève : calcul du RPO et du RTO, porte bloquante, synthèse."""
import json
import tempfile
import unittest
from pathlib import Path

import rapport_releve as r

SEUILS = r.Seuils(rto_annonce_s=8 * 3600, rpo_annonce_s=4 * 3600, rto_objectif_s=4 * 3600, rpo_objectif_s=900)
PERTE = 1_000_000


def essai(dossier: Path, ecritures: dict[int, int], restaurees: list[int], controles_ok=True,
          service_verifie=PERTE + 120_000) -> Path:
    (dossier / "temoin").mkdir(parents=True)
    (dossier / "chronologie.csv").write_text(
        f"ecriture_continue_demarree,{PERTE - 90_000}\nperte_site_primaire,{PERTE}\n"
        f"service_retabli,{PERTE + 60_000}\nservice_verifie,{service_verifie}\n")
    lignes = ["numero,acquitte_ms,trousse"] + [f"{n},{t},{n + 100}" for n, t in ecritures.items()]
    (dossier / "temoin" / "ecritures.csv").write_text("\n".join(lignes) + "\n")
    (dossier / "temoin" / "ecritures-refusees.csv").write_text(f"{max(ecritures) + 1},{PERTE + 500}\n")
    (dossier / "battements-restaures.txt").write_text("".join(f"{n}\n" for n in restaurees))
    (dossier / "controles.jsonl").write_text(json.dumps({"id": "V1", "libelle": "x", "attendu": "1",
                                                         "obtenu": "1" if controles_ok else "0", "ok": controles_ok}) + "\n")
    return dossier


class MesureTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.dossier = Path(self.tmp.name) / "s"

    def tearDown(self):
        self.tmp.cleanup()

    def test_aucune_perte_donne_un_rpo_nul(self):
        essai(self.dossier, {1: PERTE - 3000, 2: PERTE - 2000}, [1, 2])
        m, _, _ = r.mesurer(self.dossier, "replique", SEUILS)
        self.assertEqual(0.0, m.ecritures.rpo_s)
        self.assertEqual(0, m.ecritures.perdues)
        self.assertEqual(1, m.ecritures.refusees)
        self.assertTrue(m.reussi)

    def test_rpo_entre_derniere_acquittee_et_derniere_restauree(self):
        essai(self.dossier, {1: PERTE - 50_000, 2: PERTE - 40_000, 3: PERTE - 1_000}, [1, 2])
        m, _, _ = r.mesurer(self.dossier, "sauvegarde", SEUILS)
        self.assertEqual(39.0, m.ecritures.rpo_s)
        self.assertEqual(1, m.ecritures.perdues)
        self.assertEqual(3, m.ecritures.derniere_acquittee)
        self.assertEqual(2, m.ecritures.derniere_restauree)

    def test_ecriture_non_acquittee_mais_restauree_ne_compte_pas(self):
        essai(self.dossier, {1: PERTE - 2000}, [1, 2])
        m, _, _ = r.mesurer(self.dossier, "replique", SEUILS)
        self.assertEqual(0.0, m.ecritures.rpo_s)

    def test_rien_de_restaure_remonte_au_debut_des_ecritures(self):
        essai(self.dossier, {1: PERTE - 80_000, 2: PERTE - 1000}, [])
        m, _, _ = r.mesurer(self.dossier, "sauvegarde", SEUILS)
        self.assertEqual(80.0, m.ecritures.rpo_s)

    def test_rto_mesure_depuis_la_perte(self):
        essai(self.dossier, {1: PERTE - 1000}, [1])
        m, _, _ = r.mesurer(self.dossier, "replique", SEUILS)
        self.assertEqual(60.0, m.rto_service_s)
        self.assertEqual(120.0, m.rto_verifie_s)

    def test_rpo_au_dela_de_l_objectif_echoue_sans_depasser_le_niveau_annonce(self):
        essai(self.dossier, {1: PERTE - 2_000_000, 2: PERTE - 1000}, [1])
        m, _, _ = r.mesurer(self.dossier, "sauvegarde", SEUILS)
        self.assertTrue(m.porte_annoncee)
        self.assertFalse(m.porte_objectif)
        self.assertFalse(m.reussi)

    def test_rto_au_dela_du_niveau_annonce_echoue(self):
        essai(self.dossier, {1: PERTE - 1000}, [1], service_verifie=PERTE + 9 * 3600 * 1000)
        m, _, _ = r.mesurer(self.dossier, "replique", SEUILS)
        self.assertFalse(m.porte_annoncee)
        self.assertFalse(m.reussi)

    def test_un_controle_en_echec_fait_echouer_l_essai(self):
        essai(self.dossier, {1: PERTE - 1000}, [1], controles_ok=False)
        m, _, _ = r.mesurer(self.dossier, "replique", SEUILS)
        self.assertTrue(m.porte_annoncee and m.porte_objectif)
        self.assertFalse(m.reussi)

    def test_commande_scenario_ecrit_rapport_et_mesures(self):
        essai(self.dossier, {1: PERTE - 1000}, [1])
        code = r.main(["scenario", str(self.dossier), "--scenario", "replique", "--run", "https://exemple/run/1"])
        self.assertEqual(0, code)
        rapport = (self.dossier / "rapport.md").read_text()
        self.assertIn("RÉUSSI", rapport)
        self.assertIn("**Perte du site primaire**", rapport)
        mesures = json.loads((self.dossier / "mesures.json").read_text())
        self.assertEqual("https://exemple/run/1", mesures["run"])

    def test_synthese_echoue_si_un_scenario_echoue(self):
        essai(self.dossier, {1: PERTE - 1000}, [1])
        r.main(["scenario", str(self.dossier), "--scenario", "replique"])
        autre = essai(Path(self.tmp.name) / "b", {1: PERTE - 1000}, [1], controles_ok=False)
        r.main(["scenario", str(autre), "--scenario", "sauvegarde"])
        sortie = Path(self.tmp.name) / "synthese.md"
        code = r.main(["synthese", str(sortie), str(self.dossier / "mesures.json"), str(autre / "mesures.json")])
        self.assertEqual(1, code)
        self.assertIn("| replique |", sortie.read_text())

    def test_duree_lisible(self):
        self.assertEqual("42 s", r.duree(42))
        self.assertEqual("3 min 05 s", r.duree(185))


if __name__ == "__main__":
    unittest.main()
