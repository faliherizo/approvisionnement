#!/usr/bin/env python3
"""Résumé Markdown d'un essai de charge : débit, P50/P95/P99 par requête, erreurs, UC et mémoire des conteneurs.

Lit le JTL de JMeter (lecteur CSV véritable : les messages d'échec contiennent virgules et sauts de ligne) et le relevé
de releve-conteneurs.sh. Distingue l'essai entier du palier, c'est-à-dire des seules secondes où tous les utilisateurs
virtuels étaient actifs : c'est le palier qui prouve la tenue à N utilisateurs simultanés, pas la montée.

Sort en erreur (code 1) si le palier n'a jamais atteint le nombre d'utilisateurs visé : un essai qui n'a pas eu ses
N utilisateurs actifs en même temps ne prouve rien sur N utilisateurs, quels que soient ses temps de réponse.
"""
import argparse
import csv
import math
import re
import sys
from collections import defaultdict
from pathlib import Path

TRANSACTION = re.compile(r"^\d\d - ")
UNITES = {"B": 1, "KiB": 1024, "MiB": 1024 ** 2, "GiB": 1024 ** 3, "kB": 1e3, "MB": 1e6, "GB": 1e9}


def rang(valeurs, p):
    """Percentile au rang le plus proche, comme run.sh."""
    if not valeurs:
        return 0
    v = sorted(valeurs)
    i = max(1, min(len(v), math.ceil(p / 100 * len(v))))
    return v[i - 1]


def octets(texte):
    m = re.match(r"([\d.]+)\s*([A-Za-z]+)", texte.strip())
    return float(m.group(1)) * UNITES.get(m.group(2), 1) if m else 0.0


def lire_echantillons(jtl):
    with open(jtl, newline="", encoding="utf-8") as f:
        for ligne in csv.DictReader(f):
            if TRANSACTION.match(ligne["label"]):
                continue
            yield {
                "t": int(ligne["timeStamp"]),
                "label": ligne["label"],
                "ms": int(ligne["elapsed"]),
                "ok": ligne["success"] == "true",
                "actifs": int(ligne["allThreads"] or 0),
                "code": ligne["responseCode"],
            }


def tableau(echantillons, duree_s):
    par_label = defaultdict(list)
    for e in echantillons:
        par_label[e["label"]].append(e)
    lignes = ["| Requête | Nombre | Débit (req/s) | Erreurs | P50 (ms) | P95 (ms) | P99 (ms) | Max (ms) |",
              "|---|---:|---:|---:|---:|---:|---:|---:|"]
    tous = []
    for label in sorted(par_label):
        es = par_label[label]
        ms = [e["ms"] for e in es]
        tous.extend(ms)
        err = sum(not e["ok"] for e in es)
        lignes.append(f"| `{label}` | {len(es)} | {len(es) / duree_s:.1f} | {err} ({err / len(es) * 100:.2f} %) | "
                      f"{rang(ms, 50)} | {rang(ms, 95)} | {rang(ms, 99)} | {max(ms)} |")
    err = sum(not e["ok"] for e in echantillons)
    n = len(echantillons) or 1
    lignes.append(f"| **Total** | **{len(echantillons)}** | **{len(echantillons) / duree_s:.1f}** | "
                  f"**{err} ({err / n * 100:.3f} %)** | **{rang(tous, 50)}** | **{rang(tous, 95)}** | "
                  f"**{rang(tous, 99)}** | **{max(tous) if tous else 0}** |")
    return lignes


def releve(chemin, debut, fin):
    if not chemin or not Path(chemin).exists():
        return ["_Relevé des conteneurs absent._"]
    mesures = defaultdict(list)
    with open(chemin, newline="", encoding="utf-8") as f:
        for l in csv.DictReader(f):
            t = int(l["epoch"])
            if debut <= t <= fin:
                mesures[l["conteneur"]].append((float(l["cpu_pct"] or 0), octets(l["memoire_utilisee"]),
                                                octets(l["memoire_limite"])))
    lignes = ["| Conteneur | Relevés | UC moyenne | UC P95 | UC max | Mémoire max | Limite mémoire |",
              "|---|---:|---:|---:|---:|---:|---:|"]
    for nom in sorted(mesures):
        cpu = [m[0] for m in mesures[nom]]
        mem = max(m[1] for m in mesures[nom])
        lim = max(m[2] for m in mesures[nom])
        lignes.append(f"| {nom} | {len(cpu)} | {sum(cpu) / len(cpu):.0f} % | {rang(cpu, 95):.0f} % | {max(cpu):.0f} % | "
                      f"{mem / 1024 ** 3:.2f} Gio | {lim / 1024 ** 3:.2f} Gio |")
    lignes.append("")
    lignes.append("UC en pourcentage d'un cœur (`docker stats`) : 200 % = deux vCPU pleinement occupés.")
    return lignes


def main():
    a = argparse.ArgumentParser()
    a.add_argument("resultats", help="dossier de résultats de run.sh (results.jtl, synthese.md)")
    a.add_argument("--utilisateurs", type=int, required=True)
    a.add_argument("--releve", help="CSV de releve-conteneurs.sh")
    a.add_argument("--environnement", help="fichier Markdown décrivant l'environnement (inséré tel quel)")
    a.add_argument("--run", help="adresse de l'exécution (GitHub Actions)")
    args = a.parse_args()

    r = Path(args.resultats)
    echantillons = list(lire_echantillons(r / "results.jtl"))
    if not echantillons:
        print("✗ Aucun échantillon dans le JTL.", file=sys.stderr)
        return 1
    debut = min(e["t"] for e in echantillons)
    fin = max(e["t"] + e["ms"] for e in echantillons)
    max_actifs = max(e["actifs"] for e in echantillons)
    palier = [e for e in echantillons if e["actifs"] >= args.utilisateurs]
    p_debut = min((e["t"] for e in palier), default=0)
    p_fin = max((e["t"] + e["ms"] for e in palier), default=0)
    duree_palier = max(1.0, (p_fin - p_debut) / 1000)

    md = ["# Essai de charge — résumé", ""]
    if args.run:
        md += [f"Exécution : {args.run}", ""]
    if args.environnement and Path(args.environnement).exists():
        md += [Path(args.environnement).read_text(encoding="utf-8").rstrip(), ""]
    if (r / "synthese.md").exists():
        md += ["## Porte SLO (run.sh)", "", *(r / "synthese.md").read_text(encoding="utf-8").splitlines()[2:], ""]
    md += ["## Simultanéité", "",
           f"- Utilisateurs visés : **{args.utilisateurs}** ; maximum d'utilisateurs actifs relevé par JMeter : "
           f"**{max_actifs}**.",
           f"- Palier (tous les utilisateurs actifs) : **{duree_palier:.0f} s**, {len(palier)} requêtes.",
           f"- Durée totale (montée comprise) : {(fin - debut) / 1000:.0f} s, {len(echantillons)} requêtes.", ""]
    if palier:
        md += [f"## Palier à {args.utilisateurs} utilisateurs actifs", "", *tableau(palier, duree_palier), ""]
    md += ["## Essai entier (montée comprise)", "", *tableau(echantillons, max(1.0, (fin - debut) / 1000)), ""]
    md += ["## Ressources des conteneurs (essai entier)", "", *releve(args.releve, debut // 1000, fin // 1000 + 5), ""]
    codes = defaultdict(int)
    for e in echantillons:
        if not e["ok"]:
            codes[e["code"] or "?"] += 1
    if codes:
        md += ["## Erreurs par code", "", "| Code | Nombre |", "|---|---:|",
               *[f"| {c} | {n} |" for c, n in sorted(codes.items(), key=lambda x: -x[1])], ""]

    texte = "\n".join(md) + "\n"
    (r / "resume.md").write_text(texte, encoding="utf-8")
    print(texte)
    if max_actifs < args.utilisateurs:
        print(f"✗ Simultanéité non atteinte : {max_actifs} utilisateurs actifs au plus, {args.utilisateurs} visés.",
              file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
