#!/usr/bin/env python3
"""Mesure du RTO et du RPO d'un essai de relève, porte bloquante et rapport Markdown (SEC-11).

  rapport_releve.py scenario <dossier> --scenario replique|sauvegarde [--run URL] [--environnement FICHIER]
  rapport_releve.py synthese <sortie.md> <mesures.json>... [--run URL]

Définitions (document 20 de la réponse) :
  RTO = instant où le service rétabli sur le site secondaire a passé tous les contrôles bloquants
        − instant de la perte du site primaire ;
  RPO = heure d'acquittement de la dernière écriture acquittée avant la perte
        − heure d'acquittement de la dernière écriture présente après restauration.
Les heures viennent du témoin, tenu hors des deux sites.
"""
from __future__ import annotations

import argparse
import csv
import json
import os
import sys
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path

SECONDES_PAR_HEURE = 3600
SCENARIOS = {
    "replique": "Réplique asynchrone inter-sites promue (option retenue, ADR-039)",
    "sauvegarde": "Sauvegarde de base + journaux archivés rejoués (restauration à un instant donné, voie de secours)",
}
LIBELLES_JALONS = {
    "site_primaire_pret": "Site primaire prêt (base + serveur de production)",
    "jeu_de_donnees_charge": "Jeu de données chargé",
    "stockage_replique": "Exports de preuve répliqués vers le stockage secondaire",
    "sauvegarde_de_base_complete": "Sauvegarde de base terminée (journaux archivés en continu)",
    "replique_synchronisee": "Réplique secondaire en flux continu",
    "ecriture_continue_demarree": "Écritures continues démarrées",
    "perte_site_primaire": "**Perte du site primaire** (SIGKILL base + serveur)",
    "site_primaire_detruit": "Conteneurs et volumes du site primaire supprimés",
    "debut_restauration": "Début de la reprise",
    "base_promue": "Base secondaire promue en écriture",
    "base_restauree": "Base restaurée (rejeu des journaux terminé)",
    "stockage_bascule": "Stockage secondaire disponible",
    "serveur_secondaire_demarre": "Serveur démarré sur le site secondaire",
    "service_retabli": "**Service rétabli** (connexion + liste des trousses)",
    "service_verifie": "**Service vérifié** (tous les contrôles bloquants)",
}


@dataclass(frozen=True)
class Seuils:
    rto_annonce_s: int
    rpo_annonce_s: int
    rto_objectif_s: int
    rpo_objectif_s: int

    @staticmethod
    def depuis_environnement() -> "Seuils":
        def heures(nom: str, defaut: str) -> int:
            return int(float(os.environ.get(nom, defaut)) * SECONDES_PAR_HEURE)

        return Seuils(heures("RTO_ANNONCE_H", "8"), heures("RPO_ANNONCE_H", "4"),
                      heures("RTO_OBJECTIF_H", "4"), heures("RPO_OBJECTIF_H", "0.25"))


@dataclass(frozen=True)
class Ecritures:
    acquittees: int
    refusees: int
    restaurees: int
    perdues: int
    derniere_acquittee: int
    derniere_restauree: int
    rpo_s: float


@dataclass(frozen=True)
class Mesures:
    scenario: str
    rto_service_s: float
    rto_verifie_s: float
    ecritures: Ecritures
    controles_ok: int
    controles_total: int
    porte_annoncee: bool
    porte_objectif: bool
    reussi: bool


def lire_jalons(dossier: Path) -> dict[str, int]:
    with open(dossier / "chronologie.csv", encoding="utf-8") as f:
        return {etape: int(t) for etape, t in csv.reader(f)}


def lire_ecritures(dossier: Path) -> dict[int, int]:
    with open(dossier / "temoin" / "ecritures.csv", encoding="utf-8") as f:
        return {int(r["numero"]): int(r["acquitte_ms"]) for r in csv.DictReader(f)}


def compter_lignes(chemin: Path) -> int:
    return sum(1 for _ in open(chemin, encoding="utf-8")) if chemin.exists() else 0


def mesurer_ecritures(dossier: Path, perte_ms: int) -> Ecritures:
    acquittees = lire_ecritures(dossier)
    restaurees = {int(l) for l in open(dossier / "battements-restaures.txt", encoding="utf-8") if l.strip()}
    derniere = max(acquittees)
    derniere_restauree = max(restaurees, default=0)
    perdues = sorted(n for n in acquittees if n not in restaurees)
    if not perdues:
        rpo = 0.0
    elif derniere_restauree in acquittees:
        rpo = (acquittees[derniere] - acquittees[derniere_restauree]) / 1000
    else:
        # Aucune écriture continue restaurée : la perte remonte au moins au début des écritures.
        rpo = (perte_ms - min(acquittees.values())) / 1000
    return Ecritures(len(acquittees), compter_lignes(dossier / "temoin" / "ecritures-refusees.csv"), len(restaurees),
                     len(perdues), derniere, derniere_restauree, rpo)


def lire_controles(dossier: Path) -> list[dict]:
    chemin = dossier / "controles.jsonl"
    return [json.loads(l) for l in open(chemin, encoding="utf-8")] if chemin.exists() else []


def mesurer(dossier: Path, scenario: str, seuils: Seuils) -> tuple[Mesures, dict[str, int], list[dict]]:
    jalons = lire_jalons(dossier)
    perte = jalons["perte_site_primaire"]
    rto_service = (jalons["service_retabli"] - perte) / 1000
    rto_verifie = (jalons["service_verifie"] - perte) / 1000
    ecritures = mesurer_ecritures(dossier, perte)
    controles = lire_controles(dossier)
    ok = sum(c["ok"] for c in controles)
    annoncee = rto_verifie <= seuils.rto_annonce_s and ecritures.rpo_s <= seuils.rpo_annonce_s
    objectif = rto_verifie <= seuils.rto_objectif_s and ecritures.rpo_s <= seuils.rpo_objectif_s
    reussi = annoncee and objectif and controles and ok == len(controles)
    return Mesures(scenario, rto_service, rto_verifie, ecritures, ok, len(controles), annoncee, objectif, bool(reussi)), jalons, controles


def duree(secondes: float) -> str:
    minutes, s = divmod(round(secondes), 60)
    return f"{minutes} min {s:02d} s" if minutes else f"{s} s"


def heure(ms: int) -> str:
    return datetime.fromtimestamp(ms / 1000, tz=timezone.utc).strftime("%H:%M:%S.%f")[:-3]


def verdict(ok: bool) -> str:
    return "✅ respecté" if ok else "❌ dépassé"


def rapport_scenario(m: Mesures, jalons: dict[str, int], controles: list[dict], seuils: Seuils, run: str, env: str) -> str:
    e = m.ecritures
    perte = jalons["perte_site_primaire"]
    lignes = [
        f"## Scénario « {m.scenario} » — {SCENARIOS[m.scenario]}",
        "",
        f"**Résultat : {'RÉUSSI' if m.reussi else 'ÉCHEC'}**" + (f" — exécution : {run}" if run else ""),
        "",
        "| Mesure | Valeur mesurée | Niveau annoncé (contrat) | Objectif de conception |",
        "|---|---:|---|---|",
        f"| RTO — service rétabli | {duree(m.rto_service_s)} | — | — |",
        f"| **RTO — service vérifié** | **{duree(m.rto_verifie_s)}** | ≤ {seuils.rto_annonce_s // 3600} h : "
        f"{verdict(m.rto_verifie_s <= seuils.rto_annonce_s)} | ≤ {seuils.rto_objectif_s // 3600} h : "
        f"{verdict(m.rto_verifie_s <= seuils.rto_objectif_s)} |",
        f"| **RPO** | **{duree(e.rpo_s)}** | ≤ {seuils.rpo_annonce_s // 3600} h : {verdict(e.rpo_s <= seuils.rpo_annonce_s)} "
        f"| ≤ {seuils.rpo_objectif_s // 60} min : {verdict(e.rpo_s <= seuils.rpo_objectif_s)} |",
        f"| Contrôles bloquants | {m.controles_ok}/{m.controles_total} | tous | tous |",
        "",
        "### Écritures continues (témoin hors site)",
        "",
        "| Écritures acquittées avant la perte | Refusées pendant la perte | Restaurées | Acquittées perdues "
        "| Dernière acquittée | Dernière restaurée |",
        "|---:|---:|---:|---:|---:|---:|",
        f"| {e.acquittees} | {e.refusees} | {e.restaurees} | {e.perdues} | n° {e.derniere_acquittee} | n° {e.derniere_restauree} |",
        "",
        "### Chronologie",
        "",
        "| Heure (UTC) | Écart à la perte | Jalon |",
        "|---|---:|---|",
    ]
    for etape, t in sorted(jalons.items(), key=lambda kv: kv[1]):
        ecart = (t - perte) / 1000
        lignes.append(f"| {heure(t)} | {'+' if ecart >= 0 else '−'}{duree(abs(ecart))} | {LIBELLES_JALONS.get(etape, etape)} |")
    lignes += ["", "### Contrôles bloquants", "", "| # | Contrôle | Attendu | Obtenu | Résultat |", "|---|---|---:|---:|---|"]
    for c in controles:
        lignes.append(f"| {c['id']} | {c['libelle']} | {c['attendu']} | {c['obtenu']} | {'✅' if c['ok'] else '❌'} |")
    if env:
        lignes += ["", env.strip()]
    return "\n".join(lignes) + "\n"


def commande_scenario(args: argparse.Namespace) -> int:
    dossier = Path(args.dossier)
    seuils = Seuils.depuis_environnement()
    m, jalons, controles = mesurer(dossier, args.scenario, seuils)
    env = Path(args.environnement).read_text(encoding="utf-8") if args.environnement else ""
    (dossier / "rapport.md").write_text(rapport_scenario(m, jalons, controles, seuils, args.run, env), encoding="utf-8")
    (dossier / "mesures.json").write_text(json.dumps({**asdict(m), "seuils": asdict(seuils), "run": args.run}, indent=2,
                                                     ensure_ascii=False), encoding="utf-8")
    print(f"{m.scenario} : RTO vérifié {duree(m.rto_verifie_s)}, RPO {duree(m.ecritures.rpo_s)}, "
          f"contrôles {m.controles_ok}/{m.controles_total} → {'RÉUSSI' if m.reussi else 'ÉCHEC'}")
    return 0 if m.reussi else 1


def commande_synthese(args: argparse.Namespace) -> int:
    mesures = [json.loads(Path(p).read_text(encoding="utf-8")) for p in args.mesures]
    lignes = [
        "# Essai de relève — synthèse (SEC-11)",
        "",
        f"Exécution : {args.run}" if args.run else "",
        "",
        "| Scénario | RTO vérifié | RPO | Écritures perdues | Contrôles | Niveaux annoncés (8 h / 4 h) | Objectifs (4 h / 15 min) |",
        "|---|---:|---:|---:|---:|---|---|",
    ]
    for m in mesures:
        e = m["ecritures"]
        lignes.append(f"| {m['scenario']} | {duree(m['rto_verifie_s'])} | {duree(e['rpo_s'])} | {e['perdues']}/{e['acquittees']} "
                      f"| {m['controles_ok']}/{m['controles_total']} | {verdict(m['porte_annoncee'])} | {verdict(m['porte_objectif'])} |")
    for p in args.mesures:
        rapport = Path(p).with_name("rapport.md")
        if rapport.exists():
            lignes += ["", rapport.read_text(encoding="utf-8")]
    Path(args.sortie).write_text("\n".join(lignes) + "\n", encoding="utf-8")
    return 0 if mesures and all(m["reussi"] for m in mesures) else 1


def main(argv: list[str]) -> int:
    parseur = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sous = parseur.add_subparsers(dest="commande", required=True)
    s = sous.add_parser("scenario")
    s.add_argument("dossier")
    s.add_argument("--scenario", choices=sorted(SCENARIOS), required=True)
    s.add_argument("--run", default="")
    s.add_argument("--environnement", default="")
    s.set_defaults(action=commande_scenario)
    y = sous.add_parser("synthese")
    y.add_argument("sortie")
    y.add_argument("mesures", nargs="+")
    y.add_argument("--run", default="")
    y.set_defaults(action=commande_synthese)
    args = parseur.parse_args(argv)
    return args.action(args)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
