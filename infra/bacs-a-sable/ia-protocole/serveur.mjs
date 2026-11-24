// Bac à sable Ollama au niveau HTTP : imite le sous-ensemble de l'API qu'emploie la plateforme (POST /api/generate sans
// flux, GET /api/tags, HEAD /). Il exerce le vrai adaptateur OllamaService là où un modèle réel est trop lent pour les
// délais du serveur — les exécuteurs de CI. Il ne juge pas la qualité d'une rédaction : le service Ollama du bac à sable
// (`ia`) et la recette sur l'Ollama d'exploitation le font. Aucune dépendance : node serveur.mjs. Variable : PORT.
import { createServer } from 'node:http'

const PORT = Number(process.env.PORT || 11434)
const MODELE = 'contrat-banc'

function repondre(res, statut, corps) {
  res.writeHead(statut, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(corps === undefined ? '' : JSON.stringify(corps))
}

async function lireCorps(req) {
  const morceaux = []
  for await (const m of req) morceaux.push(m)
  return morceaux.length ? JSON.parse(Buffer.concat(morceaux).toString('utf8')) : {}
}

createServer(async (req, res) => {
  const chemin = new URL(req.url, 'http://bac').pathname
  if (req.method === 'HEAD' && chemin === '/') return repondre(res, 200)
  if (req.method === 'GET' && chemin === '/api/tags') return repondre(res, 200, { models: [{ name: MODELE, model: MODELE }] })
  if (req.method === 'POST' && chemin === '/api/generate') {
    let corps
    try { corps = await lireCorps(req) } catch { return repondre(res, 400, { error: 'corps JSON invalide' }) }
    if (!corps.model || typeof corps.prompt !== 'string') return repondre(res, 400, { error: 'model et prompt requis' })
    if (corps.stream !== false) return repondre(res, 400, { error: 'seul le mode sans flux est imité' })
    const max = Number(corps.options?.num_predict ?? 512)
    // Réponse déterministe, tirée de la consigne : la plateforme doit la rendre telle quelle, bornée par num_predict.
    const consigne = corps.prompt.split('Instruction : ').pop().trim().slice(0, 120)
    const mots = `Proposition du banc d'essai pour : ${consigne}. Le prestataire s'engage à exécuter le contrat conformément aux documents d'appel d'offres.`.split(' ')
    const texte = mots.slice(0, Math.max(1, max)).join(' ')
    return repondre(res, 200, { model: corps.model, created_at: new Date().toISOString(), response: texte, done: true,
      done_reason: 'stop', eval_count: Math.min(mots.length, max), prompt_eval_count: corps.prompt.split(/\s+/).length })
  }
  return repondre(res, 404, { error: `${req.method} ${chemin} : non imité` })
}).listen(PORT, () => console.log(`Bac à sable Ollama (protocole) sur :${PORT}`))
