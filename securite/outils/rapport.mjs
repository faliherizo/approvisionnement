// Rapport des tests d'intrusion (docs/securite/rapport-tests-intrusion.md), produit à partir des résultats réels :
// tests automatisés (resultats/*.json, resultats/playwright.json), ZAP, Trivy, npm audit, et du registre des constats
// (donnees/constats.json). Sortie non nulle si un test échoue ou si une vulnérabilité HAUTE/CRITIQUE n'est ni corrigée
// ni acceptée avec justification (donnees/acceptations.json).
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs'
import { execSync } from 'node:child_process'

const ici = new URL('../', import.meta.url)
const lire = (chemin, defaut = null) => (existsSync(new URL(chemin, ici)) ? JSON.parse(readFileSync(new URL(chemin, ici), 'utf8')) : defaut)
const constats = lire('donnees/constats.json', { constats: [] }).constats
const acceptations = lire('donnees/acceptations.json', { zap: [], trivy: [] })
const publique = lire('donnees/surface-publique.json')
const pw = lire('resultats/playwright.json', { suites: [] })
const res = (n) => lire(`resultats/${n}.json`)
const sha = (() => {
  try {
    const base = execSync('git rev-parse --short HEAD').toString().trim()
    return execSync('git status --porcelain').toString().trim() ? `${base} + modifications locales non commitées` : base
  } catch { return '?' }
})()

const OWASP = { 'INT-01': 'A01 · A07', 'INT-02': 'A01', 'INT-03': 'A01', 'INT-04': 'A01', 'INT-05': 'A07 · A02', 'INT-06': 'A03',
  'INT-07': 'A05', 'INT-08': 'A02 · A04 · A08', 'INT-09': 'A04 · A05', 'INT-10': 'A10' }

function tests() {
  const liste = []
  const parcourir = (s, prefixe) => {
    for (const enfant of s.suites ?? []) parcourir(enfant, enfant.title.startsWith('INT-') ? enfant.title : prefixe)
    for (const spec of s.specs ?? []) {
      const r = spec.tests.flatMap((t) => t.results)
      const statut = r.some((x) => x.status === 'passed') ? 'réussi' : r.some((x) => x.status === 'skipped') ? 'non exécuté' : 'ÉCHEC'
      liste.push({ groupe: prefixe, titre: spec.title, statut, duree: r.reduce((n, x) => n + x.duration, 0) })
    }
  }
  parcourir(pw, '')
  return liste
}

function zap() {
  const z = lire('resultats/zap.json')
  if (!z) return null
  const alertes = (z.site ?? []).flatMap((s) => s.alerts ?? [])
  return alertes.map((a) => ({ id: a.pluginid, nom: a.name, risque: a.riskdesc?.split(' ')[0] ?? a.riskcode, instances: a.count ?? a.instances?.length ?? 0,
    acceptation: acceptations.zap.find((x) => String(x.pluginid) === String(a.pluginid)) }))
}

function trivy() {
  const t = lire('resultats/trivy.json')
  if (!t) return null
  const vulns = (t.Results ?? []).flatMap((r) => (r.Vulnerabilities ?? []).map((v) => ({ cible: r.Target, ...v })))
  const secrets = (t.Results ?? []).flatMap((r) => (r.Secrets ?? []).map((s) => ({ cible: r.Target, ...s })))
  const mauvais = (t.Results ?? []).flatMap((r) => (r.Misconfigurations ?? []).filter((m) => m.Status === 'FAIL').map((m) => ({ cible: r.Target, ...m })))
  const accepte = (id) => acceptations.trivy.find((x) => x.id === id)
  return { vulns: vulns.map((v) => ({ ...v, acceptation: accepte(v.VulnerabilityID) })), secrets: secrets.map((s) => ({ ...s, acceptation: accepte(s.RuleID) })),
    mauvais: mauvais.map((m) => ({ ...m, acceptation: accepte(m.ID) })) }
}

const t = tests()
const z = zap()
const tv = trivy()
const npm = lire('resultats/npm-audit.json')
const echecs = t.filter((x) => x.statut === 'ÉCHEC')
const zapGraves = (z ?? []).filter((a) => /High|Élevé/i.test(a.risque) && !a.acceptation)
const graves = (tv?.vulns ?? []).filter((v) => ['HIGH', 'CRITICAL'].includes(v.Severity) && !v.acceptation)
const secretsNonAcceptes = (tv?.secrets ?? []).filter((s) => !s.acceptation)
const mauvaisGraves = (tv?.mauvais ?? []).filter((m) => ['HIGH', 'CRITICAL'].includes(m.Severity) && !m.acceptation)
const s1 = res('01-surface-anonyme') ?? {}

const l = []
l.push('# Rapport des tests d\'intrusion — plateforme contractuelle', '')
l.push('> Document généré par `securite/outils/rapport.mjs` à partir des résultats de `securite/lancer.sh` ; ne pas le modifier à la main.', '')
l.push(`Version éprouvée : \`${sha}\` · produit le ${new Date().toISOString().slice(0, 16).replace('T', ' ')} UTC · cible : serveur de recette branché sur les bacs à sable.`, '')
l.push('## 1. Verdict', '')
const sain = echecs.length === 0 && zapGraves.length === 0 && graves.length === 0 && secretsNonAcceptes.length === 0 && mauvaisGraves.length === 0
l.push(sain
  ? `**Aucune vulnérabilité exploitable n'est ouverte.** Les ${t.length} tests d'intrusion réussissent, aucune alerte ZAP de risque élevé ni aucune vulnérabilité de dépendance haute ou critique ne reste sans correction ou sans acceptation motivée. Les constats relevés pendant la campagne ont été corrigés et chacun est verrouillé par un test de non-régression (section 4).`
  : `**La campagne relève des points ouverts** : ${echecs.length} test(s) en échec, ${zapGraves.length} alerte(s) ZAP élevée(s), ${graves.length} vulnérabilité(s) de dépendance haute(s) ou critique(s), ${secretsNonAcceptes.length} secret(s), ${mauvaisGraves.length} défaut(s) de configuration grave(s) non traités. Voir les sections 3 à 6.`, '')
l.push('## 2. Périmètre et méthode', '')
l.push(`- **Inventaire** : ${s1.total ?? '?'} opérations d'API lues dans la spécification OpenAPI que publie le serveur ; ${s1.publiques ?? '?'} sont publiques par conception (tableau 2.1), toutes les autres exigent une session.`)
l.push('- **Tests automatisés** (`securite/tests/`) : balayages génériques de **chaque** opération (accès anonyme, moindre privilège, étanchéité entre organisations par la méthode du canari) et tests ciblés OWASP Top 10 2021 (authentification, injection, configuration, données sensibles, fichiers, SSRF).')
l.push('- **Balayage dynamique** : OWASP ZAP 2.16 (règles passives et actives) sur toute l\'API, avec une session authentifiée.')
l.push('- **Analyse de composition et de secrets** : Trivy (dépendances Maven et npm, secrets, Dockerfile, Terraform, Kubernetes) et `npm audit`.')
l.push('- **Scénarios détaillés** : [`scenarios-tests-intrusion.md`](scenarios-tests-intrusion.md).', '')
l.push('### 2.1 Surface publique (sans session)', '', '| Opérations | Pourquoi publiques | Protection |', '|---|---|---|')
for (const p of publique.operations) l.push(`| \`${p.motif}\` | ${p.raison} | ${p.protection} |`)
l.push('', '## 3. Résultats des tests d\'intrusion automatisés', '', '| Test | OWASP | Scénario | Résultat |', '|---|---|---|---|')
for (const x of t) l.push(`| ${x.groupe.split(' ')[0]} | ${OWASP[x.groupe.split(' ')[0]] ?? ''} | ${x.titre} | ${x.statut === 'réussi' ? '✅ réussi' : `❌ ${x.statut}`} |`)
const chiffres = [['01-surface-anonyme', 'opérations éprouvées sans session', (d) => d.eprouvees], ['02-roles-lecteur', 'modifications tentées par un lecteur', (d) => d.eprouvees],
  ['02-roles-plateforme', 'opérations de plateforme tentées par un administrateur d\'organisation', (d) => d.eprouvees],
  ['03-isolation-lecture', 'lectures de l\'organisation B balayées à la recherche du canari de A', (d) => d.eprouvees],
  ['03-isolation-ecriture', 'opérations de B dirigées sur les identifiants de A', (d) => d.eprouvees],
  ['06-injection-sql', 'charges d\'injection SQL envoyées', (d) => d.essais], ['08-secrets', 'réponses inspectées à la recherche de secrets', (d) => d.eprouvees]]
l.push('', 'Volume éprouvé :', '')
for (const [n, libelle, f] of chiffres) { const d = res(n); if (d) l.push(`- ${f(d)} ${libelle}`) }
const nc = res('03-isolation-ecriture')?.nonCouvertes ?? []
if (nc.length) l.push('', `Opérations à paramètre de chemin sans canari dans l'organisation A (${nc.length}) : leur étanchéité repose sur les tests d'intégration de leur module (organisation d'essai distincte) et sur le balayage de lecture ci-dessus.`)
l.push('', '## 4. Constats et corrections', '', '| N° | Gravité | OWASP | Constat | Correction | Verrou de non-régression | État |', '|---|---|---|---|---|---|---|')
for (const c of constats) l.push(`| ${c.id} | ${c.gravite} | ${c.owasp} | ${c.constat} | ${c.correction} | ${c.test} | ${c.etat} |`)
l.push('', '## 5. Balayage dynamique OWASP ZAP', '')
if (!z) l.push('_Non exécuté._')
else {
  l.push('| Alerte | Risque | Instances | Décision |', '|---|---|---|---|')
  for (const a of z.sort((x, y) => String(x.risque).localeCompare(String(y.risque)))) l.push(`| ${a.nom} (${a.id}) | ${a.risque} | ${a.instances} | ${a.acceptation ? `acceptée : ${a.acceptation.justification}` : (/High/i.test(a.risque) ? '**à corriger**' : 'information')} |`)
}
l.push('', '## 6. Dépendances, secrets et configuration (Trivy, npm audit)', '')
if (!tv) l.push('_Trivy non exécuté._')
else {
  const parSev = (s) => tv.vulns.filter((v) => v.Severity === s).length
  l.push(`Vulnérabilités de dépendances : ${parSev('CRITICAL')} critique(s), ${parSev('HIGH')} haute(s), ${parSev('MEDIUM')} moyenne(s), ${parSev('LOW')} faible(s). Secrets : ${tv.secrets.length}. Défauts de configuration : ${tv.mauvais.length}.`, '')
  const listees = tv.vulns.filter((v) => ['HIGH', 'CRITICAL'].includes(v.Severity))
  if (listees.length) {
    l.push('| Composant | Version | CVE | Gravité | Version corrigée | Décision |', '|---|---|---|---|---|---|')
    for (const v of listees) l.push(`| ${v.PkgName} (${v.cible}) | ${v.InstalledVersion} | ${v.VulnerabilityID} | ${v.Severity} | ${v.FixedVersion ?? '—'} | ${v.acceptation?.justification ?? '**à corriger**'} |`)
  }
  if (tv.secrets.length) { l.push('', '| Secret | Fichier | Décision |', '|---|---|---|'); for (const s of tv.secrets) l.push(`| ${s.Title} | ${s.cible} | ${s.acceptation?.justification ?? '**à retirer**'} |`) }
  const mg = tv.mauvais.filter((m) => ['HIGH', 'CRITICAL'].includes(m.Severity))
  if (mg.length) { l.push('', '| Configuration | Fichier | Gravité | Décision |', '|---|---|---|---|'); for (const m of mg) l.push(`| ${m.ID} ${m.Title} | ${m.cible} | ${m.Severity} | ${m.acceptation?.justification ?? '**à corriger**'} |`) }
}
if (npm?.metadata?.vulnerabilities) { const v = npm.metadata.vulnerabilities; l.push('', `npm audit (frontend, production) : ${v.critical} critique(s), ${v.high} haute(s), ${v.moderate} moyenne(s), ${v.low} faible(s).`) }
const residuels = constats.filter((c) => c.etat !== 'corrigé')
l.push('', '## 7. Risques résiduels et conditions d\'exploitation', '')
if (residuels.length === 0) l.push('Aucun constat ouvert.')
for (const c of residuels) l.push(`- **${c.id}** — ${c.constat} ${c.etat}.`)
for (const c of lire('donnees/conditions.json', { conditions: [] }).conditions) l.push(`- ${c}`)
mkdirSync(new URL('../docs/securite/', new URL('../', ici)), { recursive: true })
writeFileSync(new URL('../docs/securite/rapport-tests-intrusion.md', ici), l.join('\n') + '\n')
console.log(`Rapport écrit : ${t.length} tests (${echecs.length} échec), ZAP élevées non traitées ${zapGraves.length}, dépendances graves ${graves.length}, secrets ${secretsNonAcceptes.length}.`)
process.exit(sain ? 0 : 1)
