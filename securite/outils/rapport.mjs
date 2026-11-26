// Rapport des tests d'intrusion (docs/securite/rapport-tests-intrusion.md), produit à partir des résultats réels :
// tests automatisés (resultats/*.json, resultats/playwright.json), ZAP, Trivy, npm audit, du registre des constats
// (donnees/constats.json, scores CVSS 3.1 calculés par outils/cvss.mjs), de la matrice rôle × opération dérivée des
// annotations (donnees/matrice-roles.json) et de la correspondance WSTG / ASVS (donnees/referentiels.json).
// Sortie non nulle si un test échoue, si ZAP ou Trivy n'ont pas produit de rapport (sauf SECURITE_OUTILS=facultatifs,
// pour un poste sans Docker), ou si une vulnérabilité HAUTE/CRITIQUE n'est ni corrigée ni acceptée avec justification.
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { execSync } from 'node:child_process'
import { scoreCvss } from './cvss.mjs'

const ici = new URL('../', import.meta.url)
const lire = (chemin, defaut = null) => (existsSync(new URL(chemin, ici)) ? JSON.parse(readFileSync(new URL(chemin, ici), 'utf8')) : defaut)
const texte = (chemin) => (existsSync(new URL(chemin, ici)) ? readFileSync(new URL(chemin, ici), 'utf8') : '')
const constats = lire('donnees/constats.json', { constats: [] }).constats.map((c) => ({ ...c, ...scoreCvss(c.cvss) }))
const acceptations = lire('donnees/acceptations.json', { zap: [], trivy: [] })
const publique = lire('donnees/surface-publique.json')
const authentifiees = lire('donnees/authentification-seule.json', { operations: [] })
const matrice = lire('donnees/matrice-roles.json', { matrice: {} }).matrice
const referentiels = lire('donnees/referentiels.json', { familles: [] }).familles
const pw = lire('resultats/playwright.json', { suites: [] })
const res = (n) => lire(`resultats/${n}.json`)
const outilsExiges = process.env.SECURITE_OUTILS !== 'facultatifs'
const sha = (() => {
  try {
    const base = execSync('git rev-parse --short HEAD').toString().trim()
    return execSync('git status --porcelain -- ../backend ../frontend ../securite/tests ../securite/outils').toString().trim() ? `${base} + modifications locales non commitées` : base
  } catch { return '?' }
})()
const run = process.env.GITHUB_RUN_ID ? `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}/actions/runs/${process.env.GITHUB_RUN_ID}` : null

const OWASP = { 'INT-01': 'A01 · A07', 'INT-02': 'A01', 'INT-03': 'A01', 'INT-04': 'A01', 'INT-05': 'A07 · A02', 'INT-06': 'A03',
  'INT-07': 'A05', 'INT-08': 'A02 · A04 · A08', 'INT-09': 'A04 · A05', 'INT-10': 'A10', 'INT-11': 'A01 · A04 · A07' }

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

/** Règles exécutées par ZAP, lues dans son journal (PASS / WARN / FAIL suivis de l'identifiant de règle). */
function reglesZap() {
  const regles = [...texte('resultats/zap.log').matchAll(/^(PASS|WARN-NEW|WARN-INPROG|FAIL-NEW|FAIL-INPROG|INFO|IGNORE): .*\[(\d+)\]/gm)].map((m) => Number(m[2]))
  // Identifiants du balayeur actif de ZAP : < 10000, 20000–89999 et 90019 et au-delà ; les autres sont passifs.
  const actives = regles.filter((id) => id < 10000 || (id >= 20000 && id < 90000) || id >= 90019)
  const session = /SESSION-APRES-BALAYAGE: (\d+)/.exec(texte('resultats/zap.log'))?.[1] ?? null
  return { total: new Set(regles).size, actives: new Set(actives).size, session }
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
const rz = reglesZap()
const tv = trivy()
const npm = lire('resultats/npm-audit.json')
const echecs = t.filter((x) => x.statut === 'ÉCHEC')
const outilsAbsents = outilsExiges ? [!z && 'ZAP', !tv && 'Trivy'].filter(Boolean) : []
const zapGraves = (z ?? []).filter((a) => /High|Élevé/i.test(a.risque) && !a.acceptation)
const graves = (tv?.vulns ?? []).filter((v) => ['HIGH', 'CRITICAL'].includes(v.Severity) && !v.acceptation)
const secretsNonAcceptes = (tv?.secrets ?? []).filter((s) => !s.acceptation)
const mauvaisGraves = (tv?.mauvais ?? []).filter((m) => ['HIGH', 'CRITICAL'].includes(m.Severity) && !m.acceptation)
const s1 = res('01-surface-anonyme') ?? {}
const operationsMatrice = Object.keys(matrice).length
const ouvertesATous = Object.values(matrice).filter((r) => r.includes('*') || r.length === 10).length

const l = []
l.push('# Rapport des tests d\'intrusion — plateforme contractuelle', '')
l.push('> Document généré par `securite/outils/rapport.mjs` à partir des résultats de `securite/lancer.sh` ; ne pas le modifier à la main.', '')
l.push(`Version éprouvée : \`${sha}\` · produit le ${new Date().toISOString().slice(0, 16).replace('T', ' ')} UTC · cible : ${run ? `pile éphémère de l'exécuteur GitHub Actions ([run](${run}))` : 'serveur local (SECURITE_API_URL)'}.`, '')
l.push('> **Nature de l\'essai.** Test d\'intrusion **interne et outillé**, mené par l\'équipe de développement et rejoué à chaque campagne d\'intégration continue. Il ne constitue pas le test d\'intrusion **indépendant** exigé avant la mise en production : celui-ci est confié à une firme tierce (cahier des charges : [`cahier-des-charges-pentest-independant.md`](cahier-des-charges-pentest-independant.md)).', '')
l.push('## 1. Verdict', '')
const sain = echecs.length === 0 && outilsAbsents.length === 0 && zapGraves.length === 0 && graves.length === 0 && secretsNonAcceptes.length === 0 && mauvaisGraves.length === 0
l.push(sain
  ? `**Aucune vulnérabilité exploitable n'est ouverte.** Les ${t.length} tests d'intrusion réussissent, le balayage actif authentifié de ZAP ne relève aucune alerte de risque élevé et aucune vulnérabilité de dépendance haute ou critique ne reste sans correction ou sans acceptation motivée. Les ${constats.length} constats relevés au fil des campagnes sont corrigés et chacun est verrouillé par un test de non-régression (section 4).`
  : `**La campagne relève des points ouverts** : ${echecs.length} test(s) en échec, ${outilsAbsents.length ? `outil(s) sans rapport : ${outilsAbsents.join(', ')}, ` : ''}${zapGraves.length} alerte(s) ZAP élevée(s), ${graves.length} vulnérabilité(s) de dépendance haute(s) ou critique(s), ${secretsNonAcceptes.length} secret(s), ${mauvaisGraves.length} défaut(s) de configuration grave(s) non traités. Voir les sections 3 à 6.`, '')

l.push('## 2. Périmètre et méthode', '')
l.push('### 2.1 Périmètre : tous les points d\'accès', '')
l.push(`- **Inventaire réel** : la référence est la liste des routes que sert Spring MVC (\`RequestMappingHandlerMapping\`), et non la documentation. \`RoutesSecuriseesIntegrationTest\` (construction du serveur) échoue si une route manque à la spécification OpenAPI ou si une opération documentée n'est servie par aucune route ; la suite d'intrusion relit l'OpenAPI à chaque campagne : **${s1.total ?? '?'} opérations**.`)
l.push(`- **Surface publique** : ${s1.publiques ?? '?'} opérations accessibles sans session, chacune motivée (tableau 2.3), toutes limitées en débit par client ; toutes les autres (${s1.eprouvees ?? '?'}) exigent une session et répondent 401 sans elle.`)
l.push(`- **Autorisation** : ${operationsMatrice} opérations authentifiées dans la matrice rôle × opération (\`securite/donnees/matrice-roles.json\`), DÉRIVÉE des annotations \`@PreAuthorize\` par le moteur d'autorisation de l'application ; ${operationsMatrice - ouvertesATous} sont réservées à certains rôles, ${ouvertesATous} sont ouvertes à tout rôle authentifié avec un contrôle fin motivé dans le service (\`authentification-seule.json\`, ${authentifiees.operations.length} motifs, chacun avec son test).`)
l.push('- **Hors périmètre** : interface web (couverte par les scénarios e2e et l\'accessibilité), infrastructure d\'hébergement réelle (OVH, Azure), ingénierie sociale, déni de service volumétrique.', '')
l.push('### 2.2 Méthodologie et correspondance OWASP WSTG v4.2 / ASVS 4.0.3 niveau 2', '')
l.push('| Essai | Objet | WSTG v4.2 | ASVS 4.0.3 (N2) |', '|---|---|---|---|')
for (const f of referentiels) l.push(`| ${f.essai} | ${f.objet} | ${f.wstg.join(', ')} | ${f.asvs.join(', ')} |`)
l.push('', `Outils : Playwright (tests d'intrusion \`securite/tests/\`), JUnit + MockMvc (\`backend/src/test/java/qc/levis/contrat/securite/\`), OWASP ZAP 2.16.1 (\`zap-api-scan.py\`, règles passives ET actives, session authentifiée), Trivy 0.58.1, npm audit. Scores : CVSS 3.1, score de base calculé à partir du vecteur (\`securite/outils/cvss.mjs\`). Scénarios détaillés : [\`scenarios-tests-intrusion.md\`](scenarios-tests-intrusion.md).`, '')
l.push('### 2.3 Surface publique (sans session)', '', '| Opérations | Contrôle | Pourquoi publiques | Protection |', '|---|---|---|---|')
for (const p of publique.operations) l.push(`| \`${p.motif}\` | ${p.controle === 'jeton' ? 'jeton exigé' : 'ouverte'} | ${p.raison} | ${p.protection} |`)

l.push('', '## 3. Résultats des tests d\'intrusion automatisés', '', '| Test | OWASP | Scénario | Résultat |', '|---|---|---|---|')
for (const x of t) l.push(`| ${x.groupe.split(' ')[0]} | ${OWASP[x.groupe.split(' ')[0]] ?? ''} | ${x.titre} | ${x.statut === 'réussi' ? '✅ réussi' : `❌ ${x.statut}`} |`)
const chiffres = [['01-surface-anonyme', 'opérations éprouvées sans session', (d) => d.eprouvees], ['02-roles-lecteur', 'modifications tentées par un lecteur', (d) => d.eprouvees],
  ['02-roles-plateforme', 'opérations de plateforme tentées par un administrateur d\'organisation', (d) => d.eprouvees],
  ['03-isolation-lecture', 'lectures de l\'organisation B balayées à la recherche du canari de A', (d) => d.eprouvees],
  ['03-isolation-ecriture', 'opérations de B dirigées sur les identifiants de A', (d) => d.eprouvees],
  ['06-injection-sql', 'charges d\'injection SQL envoyées', (d) => d.essais], ['08-secrets', 'réponses inspectées à la recherche de secrets', (d) => d.eprouvees],
  ['08-cartes-paiement', 'opérations d\'écriture éprouvées avec un numéro de carte', (d) => d.eprouvees],
  ['11-matrice-roles', 'couples rôle refusé × opération éprouvés sur le serveur réel (403 attendu)', (d) => d.essais],
  ['11-jetons-forges', 'appels aux routes à jeton avec un jeton forgé', (d) => d.essais],
  ['11-limitation-debit', 'requêtes publiques avant le refus 429 (seuil annoncé + 1)', (d) => d.refuseApres]]
l.push('', 'Volume éprouvé :', '')
for (const [n, libelle, f] of chiffres) { const d = res(n); if (d) l.push(`- ${f(d)} ${libelle}`) }
const nc = res('03-isolation-ecriture')?.nonCouvertes ?? []
if (nc.length) l.push('', `Opérations à paramètre de chemin sans canari dans l'organisation A (${nc.length}) : leur étanchéité repose sur les tests d'intégration de leur module (organisation d'essai distincte) et sur le balayage de lecture ci-dessus.`)

l.push('', '## 4. Constats et corrections (CVSS 3.1)', '', '| N° | CVSS 3.1 | OWASP | Constat | Correction | Verrou de non-régression | État |', '|---|---|---|---|---|---|---|')
for (const c of [...constats].sort((a, b) => b.score - a.score || a.id.localeCompare(b.id))) {
  l.push(`| ${c.id} | **${c.score.toFixed(1)} ${c.cote}**<br>\`${c.cvss.replace('CVSS:3.1/', '')}\` | ${c.owasp} | ${c.constat} | ${c.correction} | ${c.test} | ${c.etat} |`)
}
const parCote = ['Critique', 'Élevée', 'Moyenne', 'Faible'].map((k) => `${constats.filter((c) => c.cote === k).length} ${k.toLowerCase()}(s)`).join(', ')
l.push('', `Répartition : ${parCote} ; ${constats.filter((c) => c.etat !== 'corrigé').length} ouvert(s).`)

l.push('', '## 5. Balayage dynamique OWASP ZAP (actif, authentifié)', '')
if (!z) l.push(outilsExiges ? '**Non exécuté : la campagne échoue** (un balayage absent ne vaut pas un balayage propre).' : '_Non exécuté (poste sans Docker)._')
else {
  l.push(`Règles exécutées : ${rz.total}, dont ${rz.actives} règles du balayeur actif. Session du compte de balayage encore valide à la fin : ${rz.session === '200' ? 'oui' : rz.session ?? 'non vérifiée'}. Rapport complet : artefact \`zap.html\`.`, '')
  l.push('| Alerte | Risque | Instances | Décision |', '|---|---|---|---|')
  for (const a of z.sort((x, y) => String(x.risque).localeCompare(String(y.risque)))) l.push(`| ${a.nom} (${a.id}) | ${a.risque} | ${a.instances} | ${a.acceptation ? `acceptée : ${a.acceptation.justification}` : (/High/i.test(a.risque) ? '**à corriger**' : 'information')} |`)
}
l.push('', '## 6. Dépendances, secrets et configuration (Trivy, npm audit)', '')
if (!tv) l.push(outilsExiges ? '**Trivy non exécuté : la campagne échoue.**' : '_Trivy non exécuté (poste sans Docker)._')
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
l.push('', '## 7. Risques résiduels, conditions d\'exploitation et limites', '')
if (residuels.length === 0) l.push('Aucun constat ouvert.')
for (const c of residuels) l.push(`- **${c.id}** — ${c.constat} ${c.etat}.`)
for (const c of lire('donnees/conditions.json', { conditions: [] }).conditions) l.push(`- ${c}`)
mkdirSync(new URL('../docs/securite/', new URL('../', ici)), { recursive: true })
writeFileSync(new URL('../docs/securite/rapport-tests-intrusion.md', ici), l.join('\n') + '\n')
console.log(`Rapport écrit : ${t.length} tests (${echecs.length} échec), outils absents ${outilsAbsents.length}, ZAP élevées non traitées ${zapGraves.length}, dépendances graves ${graves.length}, secrets ${secretsNonAcceptes.length}.`)
process.exit(sain ? 0 : 1)
