import { useEffect, useState, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import api from '../api'
import AiAssistant from '../components/AiAssistant.jsx'
import DropdownMenu from '../components/DropdownMenu.jsx'
import Modal, { PromptModal } from '../components/Modal.jsx'
import RichText, { RichHtml } from '../components/RichText.jsx'
import SignaturePad from '../components/SignaturePad.jsx'
import { useToast } from '../components/Toast.jsx'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext'

// Lien public de signature (partageable au signataire externe).


export default function DocumentEditor() {
  const { id } = useParams()
  const toast = useToast()
  const { t } = useI18n()
  const [doc, setDoc] = useState(null)
  const [disponibles, setDisponibles] = useState([])
  const [selection, setSelection] = useState([])
  const [commentaires, setCommentaires] = useState([])
  const [publications, setPublications] = useState([])
  const [publication, setPublication] = useState({ cible: 'SHAREPOINT', format: 'PDF', destination: 'Contrats' })
  const [publicationEnCours, setPublicationEnCours] = useState(false)
  const [repertoires, setRepertoires] = useState([]) // répertoires de dépôt personnels de l'utilisateur
  const [etapes, setEtapes] = useState([])
  const [addendas, setAddendas] = useState([])
  const [versions, setVersions] = useState([])
  const [obligations, setObligations] = useState([])
  const [signataires, setSignataires] = useState([])
  const [annexes, setAnnexes] = useState([])
  const [bordLignes, setBordLignes] = useState([])
  const [bordTitre, setBordTitre] = useState('Bordereau de prix')
  const [diff, setDiff] = useState(null)
  const [cmp, setCmp] = useState({ a: '', b: '' })
  const [nouveauCommentaire, setNouveauCommentaire] = useState('')
  const [modal, setModal] = useState(null) // { kind }
  const [mForm, setMForm] = useState({})
  const typesObligation = useReference('TYPE_OBLIGATION')
  const [signature, setSignature] = useState(null) // { signataire }
  const [signNom, setSignNom] = useState('')
  const [signImg, setSignImg] = useState('')
  const [apercu, setApercu] = useState(null) // { url }
  const [gabarit, setGabarit] = useState('') // '' = déduit du type du document
  const [gabarits, setGabarits] = useState([]) // catalogue effectif (API)
  const [clauseEdit, setClauseEdit] = useState(null) // section en cours de rédaction
  const [changeAddenda, setChangeAddenda] = useState(null) // { addendaId, clauseId, nouveauTexte }

  // Libellés de confirmation par type de saisie.
  const MESSAGES_MODALE = {
    version: t('documenteditor.versionCapturee'),
    obligation: t('documenteditor.obligationAjoutee'),
    signataire: t('documenteditor.signataireAjoute'),
    addenda: t('documenteditor.addendaCree'),
    annexe: t('documenteditor.annexeAjoutee'),
  }

  // Configuration des modales de saisie (remplacent window.prompt).
  const MODALES = {
    version: { titre: t('documenteditor.capturerVersion'), champs: [{ name: 'note', label: t('documenteditor.noteVersion') }] },
    obligation: {
      titre: t('documenteditor.nouvelleObligation'),
      champs: [
        { name: 'titre', label: t('documenteditor.titre'), required: true },
        { name: 'dateEcheance', label: t('documenteditor.dateEcheance'), type: 'date' },
      ],
    },
    signataire: {
      titre: t('documenteditor.ajouterSignataire'),
      champs: [
        { name: 'nom', label: t('documenteditor.nom'), required: true },
        { name: 'courriel', label: t('documenteditor.courriel'), type: 'email', required: true },
        { name: 'role', label: t('documenteditor.roleSignataire') },
      ],
    },
    addenda: { titre: t('documenteditor.nouvelAddenda'), champs: [{ name: 'objet', label: t('documenteditor.objetAddenda'), required: true }] },
    annexe: {
      titre: t('documenteditor.nouvelleAnnexe'),
      champs: [
        { name: 'numero', label: t('documenteditor.numeroAnnexe') },
        { name: 'titre', label: t('documenteditor.titre'), required: true },
        { name: 'referenceClause', label: t('documenteditor.clauseReference') },
        { name: 'contenu', label: t('documenteditor.contenu'), type: 'richtext', rows: 4 },
      ],
    },
  }

  const recharger = useCallback(() => {
    api.get(`/documents/${id}`).then((r) => setDoc(r.data))
    api.get(`/documents/${id}/clauses-disponibles`).then((r) => setDisponibles(r.data))
    api.get(`/documents/${id}/commentaires`).then((r) => setCommentaires(r.data))
    api.get(`/documents/${id}/publications`).then((r) => setPublications(r.data)).catch(() => setPublications([]))
    api.get(`/documents/${id}/approbation`).then((r) => setEtapes(r.data))
    api.get(`/documents/${id}/addendas`).then((r) => setAddendas(r.data))
    api.get(`/documents/${id}/versions`).then((r) => setVersions(r.data))
    api.get(`/documents/${id}/obligations`).then((r) => setObligations(r.data))
    api.get(`/documents/${id}/signataires`).then((r) => setSignataires(r.data))
    api.get(`/documents/${id}/annexes`).then((r) => setAnnexes(r.data))
    api.get(`/documents/${id}/bordereau`).then((r) => {
      if (r.data && r.data.lignes) {
        setBordTitre(r.data.titre || 'Bordereau de prix')
        setBordLignes(r.data.lignes.map((l) => ({
          cle: crypto.randomUUID(),
          description: l.description || '', quantite: l.quantite ?? '', taux: l.taux ?? '',
        })))
      }
    })
  }, [id])

  const ouvrirModale = (kind) => { setMForm({}); setModal({ kind }) }

  /** Exécute une opération d'écriture, notifie le résultat et recharge le document. */
  async function sauver(operation, messageSucces) {
    try {
      await operation()
      recharger()
      toast.succes(messageSucces)
      return true
    } catch (e) {
      toast.erreur(e.response?.data?.message || e.response?.data?.erreur || t('documenteditor.echecEnregistrement'))
      return false
    }
  }

  async function soumettreModale() {
    const kind = modal.kind
    const ok = await sauver(async () => {
      if (kind === 'version') await api.post(`/documents/${id}/versions`, { note: mForm.note || '' })
      else if (kind === 'obligation') await api.post(`/documents/${id}/obligations`, { titre: mForm.titre, type: mForm.type || 'LIVRABLE', dateEcheance: mForm.dateEcheance || null })
      else if (kind === 'signataire') await api.post(`/documents/${id}/signataires`, { nom: mForm.nom, courriel: mForm.courriel || 'na@levis.qc.ca', role: mForm.role || '' })
      else if (kind === 'addenda') await api.post(`/documents/${id}/addendas`, { objet: mForm.objet })
      else if (kind === 'annexe') await api.post(`/documents/${id}/annexes`, {
        numero: mForm.numero || '', titre: mForm.titre,
        referenceClause: mForm.referenceClause || '', contenu: mForm.contenu || '',
      })
    }, MESSAGES_MODALE[kind] || t('documenteditor.enregistre'))
    if (ok) setModal(null)
  }

  async function supprimerAnnexe(annexeId) {
    await sauver(() => api.delete(`/documents/${id}/annexes/${annexeId}`), t('documenteditor.annexeSupprimee'))
  }

  const ajouterLigne = () =>
    setBordLignes((l) => [...l, { cle: crypto.randomUUID(), description: '', quantite: '', taux: '' }])
  const retirerLigne = (cle) => setBordLignes((l) => l.filter((ligne) => ligne.cle !== cle))
  const majLigne = (cle, champ, valeur) =>
    setBordLignes((l) => l.map((ligne) => (ligne.cle === cle ? { ...ligne, [champ]: valeur } : ligne)))
  const totalLigne = (l) => (Number(l.quantite) || 0) * (Number(l.taux) || 0)
  const totalBordereau = () => bordLignes.reduce((s, l) => s + totalLigne(l), 0)

  async function enregistrerBordereau() {
    await sauver(() => api.put(`/documents/${id}/bordereau`, {
      titre: bordTitre,
      lignes: bordLignes
        .filter((l) => l.description.trim())
        .map((l) => ({ description: l.description, quantite: Number(l.quantite) || 0, taux: Number(l.taux) || 0 })),
    }), t('documenteditor.bordereauEnregistre'))
  }

  async function comparer() {
    if (!cmp.a || !cmp.b) return
    const { data } = await api.get('/versions/comparer', { params: { a: cmp.a, b: cmp.b } })
    setDiff(data)
  }
  async function completerObligation(oid) {
    await sauver(() => api.post(`/obligations/${oid}/completer`), t('documenteditor.obligationCompletee'))
  }
  async function demarrerSignature() {
    await sauver(() => api.post(`/documents/${id}/signature/demarrer`), t('documenteditor.signatureDemarree'))
  }
  function ouvrirSignature(s) {
    setSignNom(s.nom || '')
    setSignImg('')
    setSignature({ signataire: s })
  }
  async function signerManuel() {
    const jeton = signature.signataire.jeton
    const ok = await sauver(
      () => api.post(`/signature/${jeton}/signer`, { nomComplet: signNom, signatureImage: signImg }),
      t('documenteditor.signatureApposee'))
    if (ok) setSignature(null)
  }
  /** Le presse-papier peut être refusé par le navigateur : la réussite comme l'échec sont annoncés, jamais tus. */
  function copier(texte, message) {
    const ecriture = navigator.clipboard ? navigator.clipboard.writeText(texte) : Promise.reject(new Error('presse-papier'))
    ecriture.then(() => toast.succes(message)).catch(() => toast.erreur(t('documenteditor.copieImpossible')))
  }
  function copierLien(jeton) {
    copier(`${window.location.origin}/signature/${jeton}`, t('documenteditor.lienCopie'))
  }

  const suffixeGabarit = () => (gabarit ? `?gabarit=${gabarit}` : '')

  async function exporter(format) {
    try {
      const { data } = await api.get(`/documents/${id}/export/${format}${suffixeGabarit()}`,
        { responseType: 'blob' })
      const url = URL.createObjectURL(data)
      const a = document.createElement('a')
      a.href = url
      a.download = `devis-${id}.${format}`
      a.click()
      URL.revokeObjectURL(url)
      toast.succes(t('documenteditor.exportGenere', { format: format.toUpperCase() }))
    } catch {
      toast.erreur(t('documenteditor.echecExport'))
    }
  }

  /** Fiche des métadonnées SEAO (Lévis C-20) ; un refus (numéro d'appel d'offres absent) est expliqué tel quel. */
  async function exporterSeao() {
    try {
      const { data } = await api.get(`/documents/${id}/export/seao`, { responseType: 'blob' })
      const url = URL.createObjectURL(data)
      Object.assign(document.createElement('a'), { href: url, download: `seao-devis-${id}.xml` }).click()
      URL.revokeObjectURL(url)
      toast.succes(t('documenteditor.seaoGenere'))
    } catch (e) {
      const message = e.response?.data instanceof Blob ? JSON.parse(await e.response.data.text()).message : null
      toast.erreur(message || t('documenteditor.echecExport'))
    }
  }

  async function ouvrirApercu() {
    // Aperçu image (PNG) de la première page : se rend dans tous les navigateurs (contrairement au
    // lecteur PDF natif), donc réellement visible à l'écran.
    const { data } = await api.get(`/documents/${id}/export/pdf/apercu-image${suffixeGabarit()}`,
      { responseType: 'blob' })
    setApercu({ url: URL.createObjectURL(data) })
  }
  function fermerApercu() {
    if (apercu) URL.revokeObjectURL(apercu.url)
    setApercu(null)
  }

  async function reintegrerAddenda(addendaId) {
    await sauver(() => api.post(`/addendas/${addendaId}/reintegrer`), t('documenteditor.addendaReintegre'))
  }

  // Rédaction interactive d'un addenda : modification d'une clause du devis.
  const ouvrirChangement = (addendaId) => setChangeAddenda({ addendaId, clauseId: '', nouveauTexte: '' })
  async function enregistrerChangement() {
    const c = changeAddenda
    if (!c.clauseId || !c.nouveauTexte.replace(/<[^>]*>/g, '').trim()) return
    const ok = await sauver(
      () => api.post(`/addendas/${c.addendaId}/changements`,
        { clauseId: Number(c.clauseId), nouveauTexte: c.nouveauTexte }),
      t('documenteditor.changementAjoute'))
    if (ok) setChangeAddenda(null)
  }

  useEffect(() => { recharger() }, [recharger])
  useEffect(() => { chargerRepertoires() }, []) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => { api.get('/gabarits').then((r) => setGabarits(r.data)).catch(() => setGabarits([])) }, [])

  const toggle = (cid) =>
    setSelection((s) => s.includes(cid) ? s.filter((x) => x !== cid) : [...s, cid])

  async function assembler() {
    await sauver(() => api.post(`/documents/${id}/assembler`, { clauseIds: selection }), t('documenteditor.devisAssemble'))
  }

  // --- Rédaction locale des sections (texte propre au document + sections libres) ---
  const ouvrirRedaction = (dc) => setClauseEdit({
    id: dc?.id ?? null, libre: !dc, titre: dc?.titre || '', section: dc?.section || '',
    texteEffectif: dc?.texteEffectif || '',
  })

  async function enregistrerRedaction() {
    const c = clauseEdit
    const corps = { titre: c.titre, section: c.section, texteEffectif: c.texteEffectif }
    const ok = await sauver(
      () => (c.id ? api.put(`/documents/${id}/clauses/${c.id}`, corps)
                  : api.post(`/documents/${id}/clauses`, corps)),
      c.id ? t('documenteditor.sectionMiseAJour') : t('documenteditor.sectionAjoutee'))
    if (ok) setClauseEdit(null)
  }

  async function supprimerSection(dcId) {
    await sauver(() => api.delete(`/documents/${id}/clauses/${dcId}`), t('documenteditor.sectionRetiree'))
  }

  async function commenter(e) {
    e.preventDefault()
    if (!nouveauCommentaire.replace(/<[^>]*>/g, '').trim()) return
    const ok = await sauver(
      () => api.post(`/documents/${id}/commentaires`, { corps: nouveauCommentaire }), t('documenteditor.commentaireAjoute'))
    if (ok) setNouveauCommentaire('')
  }

  async function resoudreCommentaire(commentaireId, resolu) {
    await sauver(
      () => api.post(`/documents/${id}/commentaires/${commentaireId}/resolution`, { resolu }),
      resolu ? t('documenteditor.commentaireResolu') : t('documenteditor.commentaireRouvert'))
  }

  const chargerRepertoires = () =>
    api.get('/profil/repertoires').then((r) => setRepertoires(r.data)).catch(() => {})

  async function enregistrerRepertoire() {
    const chemin = publication.destination?.trim()
    if (!chemin) return
    try {
      await api.post('/profil/repertoires', { chemin })
      chargerRepertoires()
      toast.succes(t('documenteditor.repertoireEnregistre'))
    } catch {
      toast.erreur(t('documenteditor.repertoireEchec'))
    }
  }

  async function publier(e) {
    e.preventDefault()
    setPublicationEnCours(true)
    try {
      await sauver(
        () => api.post(`/documents/${id}/publications`, publication),
        t('documenteditor.publieVers', { cible: publication.cible }) + (publication.destination ? ` (${publication.destination})` : ''))
    } finally {
      setPublicationEnCours(false)
    }
  }

  async function demarrerApprobation() {
    await sauver(() => api.post(`/documents/${id}/approbation`), t('documenteditor.approbationDemarree'))
  }

  async function decider(stepId, approuve) {
    await sauver(() => api.post(`/documents/approbation/${stepId}/decision`, { approuve, note: '' }),
      approuve ? t('documenteditor.etapeApprouvee') : t('documenteditor.etapeRefusee'))
  }

  if (!doc) return <div className="panel">{t('documenteditor.chargement')}</div>

  // La publication n'est offerte qu'une fois le document validé (approuvé et au-delà).
  const publiable = ['APPROUVE', 'EN_SIGNATURE', 'SIGNE', 'PUBLIE'].includes(doc.statut)

  return (
    <div>
      <div className="doc-head panel">
        <div>
          <h1>{doc.titre}</h1>
          <div className="meta">
            <span>{doc.type}</span> · <span>{doc.modeSollicitation || '—'}</span> ·
            <span> {doc.valeurEstimee ? `${doc.valeurEstimee} $` : '—'}</span> ·
            <span className={`badge s-${doc.statut}`}>{doc.statut}</span>
          </div>
          {doc.contexte && <div className="muted"><RichHtml html={doc.contexte} /></div>}
        </div>
        <div className="actions">
          <button onClick={assembler}>{t('documenteditor.assemblerDevis')}</button>
          <button className="secondary" onClick={demarrerApprobation}>{t('documenteditor.demarrerApprobation')}</button>
          <DropdownMenu label={t('documenteditor.generer')} align="right" className="dropdown--actions">
            {(fermer) => (
              <>
                <div className="menu-groupe">
                  <label className="gabarit-select">
                    <span className="gabarit-select__libelle">{t('documenteditor.gabarit')}</span>
                    <select value={gabarit} onChange={(e) => setGabarit(e.target.value)}
                      aria-label={t('documenteditor.gabaritAriaLabel')}>
                      <option value="">{t('documenteditor.gabaritAuto')}</option>
                      {gabarits.map((g) => <option key={g.id} value={g.code}>{g.nom || g.code}</option>)}
                    </select>
                  </label>
                </div>
                <button type="button" onClick={() => { fermer(); ouvrirApercu() }}>{t('documenteditor.apercuPdf')}</button>
                <button type="button" onClick={() => { fermer(); exporter('pdf') }}>{t('documenteditor.exportPdf')}</button>
                <button type="button" onClick={() => { fermer(); exporter('docx') }}>{t('documenteditor.exportDocx')}</button>
                <button type="button" onClick={() => { fermer(); exporterSeao() }}>{t('documenteditor.exportSeao')}</button>
              </>
            )}
          </DropdownMenu>
        </div>
      </div>

      <div className="grid2">
        <section className="panel">
          <h2>{t('documenteditor.clausesCatalogue')}</h2>
          <p className="muted">{t('documenteditor.clausesCatalogueAide')}</p>
          {disponibles.map((c) => (
            <label key={c.id} className="check-row">
              <input type="checkbox"
                     checked={c.autoInclus || selection.includes(c.id)}
                     disabled={c.autoInclus}
                     onChange={() => toggle(c.id)} />
              <span className="code">{c.code}</span>
              <span className={`tag t-${c.categorie}`}>{c.categorie}</span>
              {c.autoInclus
                ? <span className="badge s-APPROUVE">{t('documenteditor.badgeAuto')}</span>
                : c.eligible && <span className="badge s-EN_ATTENTE">{t('documenteditor.badgeEligible')}</span>}
              {c.normative && <span className="tag">{t('documenteditor.tagNormative')}</span>}
              {c.titre}
            </label>
          ))}
          {disponibles.length === 0 && <p className="muted">{t('documenteditor.aucuneClause')}</p>}
        </section>

        <section className="panel">
          <div className="addenda-head">
            <h2>{t('documenteditor.devisAssembleTitre', { n: doc.clauses?.length || 0 })}</h2>
            <button className="secondary" onClick={() => ouvrirRedaction(null)}>{t('documenteditor.ajouterSectionLibre')}</button>
          </div>
          <ol className="assembled">
            {doc.clauses?.map((dc) => (
              <li key={dc.id}>
                <div className="clause-head">
                  <strong>{dc.numero ? `${dc.numero} ` : ''}{dc.titre || dc.code}</strong>
                  {dc.normative && <span className="tag" title={t('documenteditor.clauseNormativeVerrou')}>🔒 {t('documenteditor.tagNormative')}</span>}
                  {dc.libre && <span className="tag t-OPTIONNELLE">{t('documenteditor.sectionLibre')}</span>}
                  {dc.modifieLocalement && !dc.libre && <span className="tag t-STANDARD">{t('documenteditor.modifieLocalement')}</span>}
                  {!dc.normative && (
                    <span className="btns">
                      <button className="lien" onClick={() => ouvrirRedaction(dc)}>{t('documenteditor.rediger')}</button>
                      <button className="lien danger" onClick={() => supprimerSection(dc.id)}>{t('documenteditor.retirer')}</button>
                    </span>
                  )}
                </div>
                <RichHtml html={dc.texteEffectif} />
              </li>
            ))}
            {(!doc.clauses || doc.clauses.length === 0) &&
              <li className="muted">{t('documenteditor.nonAssemble')}</li>}
          </ol>
        </section>
      </div>

      <div className="grid2">
        <section className="panel">
          <h2>{t('documenteditor.circuitApprobation')}</h2>
          <ul className="steps">
            {etapes.map((s) => (
              <li key={s.id}>
                <span className="niveau">{t('documenteditor.niveau', { n: s.niveau })}</span>
                <span className="role">{s.roleRequis}</span>
                <span className={`badge s-${s.statut}`}>{s.statut}</span>
                {s.statut === 'EN_ATTENTE' && (
                  <span className="btns">
                    <button onClick={() => decider(s.id, true)}>{t('documenteditor.approuver')}</button>
                    <button className="danger" onClick={() => decider(s.id, false)}>{t('documenteditor.refuser')}</button>
                  </span>
                )}
              </li>
            ))}
            {etapes.length === 0 && <li className="muted">{t('documenteditor.aucuneEtape')}</li>}
          </ul>
        </section>

        <section className="panel">
          <h2>{t('documenteditor.commentaires')}</h2>
          <div className="comments">
            {commentaires.map((c) => (
              <div key={c.id} className={`comment${c.resolu ? ' resolu' : ''}${c.parentId ? ' reponse' : ''}`}>
                <strong>{c.auteur}</strong>
                {c.resolu && <span className="badge s-APPROUVE"> {t('documenteditor.resolu')}{c.resoluPar ? ` · ${c.resoluPar}` : ''}</span>}
                <RichHtml html={c.corps} />
                <button type="button" className="secondary"
                        onClick={() => resoudreCommentaire(c.id, !c.resolu)}>
                  {c.resolu ? t('documenteditor.rouvrir') : t('documenteditor.marquerResolu')}
                </button>
              </div>
            ))}
            {commentaires.length === 0 && <p className="muted">{t('documenteditor.aucunCommentaire')}</p>}
          </div>
          <form onSubmit={commenter} className="form">
            <RichText value={nouveauCommentaire} onChange={setNouveauCommentaire}
                      rows={2} placeholder={t('documenteditor.commenterPlaceholder')} />
            <button type="submit">{t('documenteditor.commenter')}</button>
          </form>
        </section>

        <section className="panel" aria-labelledby="titre-publication">
          <h2 id="titre-publication">{t('documenteditor.publicationTitre')}</h2>
          {!publiable ? (
            <p className="muted" role="status">
              {t('documenteditor.publicationVerrouillee1')}
              <strong> {t('documenteditor.valide')}</strong>{' '}
              {t('documenteditor.publicationVerrouillee2')} <strong>{doc.statut}</strong>.
            </p>
          ) : (
          <>
          <p className="muted" id="aide-destination">{t('documenteditor.publicationAide')}</p>
          <form onSubmit={publier} className="form" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <label>{t('documenteditor.cible')}
              <select value={publication.cible} onChange={(e) => setPublication({ ...publication, cible: e.target.value })}>
                <option value="SHAREPOINT">SharePoint</option>
                <option value="ONEDRIVE">OneDrive</option>
                <option value="OUTLOOK">Outlook</option>
                <option value="TEAMS">Teams</option>
                <option value="SAP">SAP</option>
              </select>
            </label>
            <label>{t('documenteditor.format')}
              <select value={publication.format} onChange={(e) => setPublication({ ...publication, format: e.target.value })}>
                <option value="PDF">PDF</option>
                <option value="DOCX">Word (.docx)</option>
              </select>
            </label>
            <label>{t('documenteditor.repertoireDestination')}
              <input type="text" value={publication.destination} placeholder={t('documenteditor.repertoirePlaceholder')}
                     list="suggestions-repertoire" aria-describedby="aide-destination"
                     onChange={(e) => setPublication({ ...publication, destination: e.target.value })} />
              <datalist id="suggestions-repertoire">
                {(repertoires.length
                  ? repertoires.map((r) => r.chemin)
                  : ['Contrats', 'Contrats/2026', 'Appels d’offres', 'Dossiers d’approvisionnement']
                ).map((chemin) => <option key={chemin} value={chemin} />)}
              </datalist>
            </label>
            <button type="button" className="secondary" onClick={enregistrerRepertoire}
                    disabled={!publication.destination?.trim()}
                    title={t('documenteditor.enregistrerRepertoireTitle')}>
              {t('documenteditor.enregistrerRepertoire')}
            </button>
            <button type="submit" disabled={publicationEnCours} aria-busy={publicationEnCours}>
              {publicationEnCours ? t('documenteditor.publicationEnCours') : t('documenteditor.publier')}
            </button>
          </form>
          {publicationEnCours && (
            <p className="muted" role="status" aria-live="polite">
              <span className="spinner" aria-hidden="true" /> {t('documenteditor.televersement', { cible: publication.cible })}
            </p>
          )}
          </>
          )}
          <table className="table" style={{ marginTop: '0.75rem' }}>
            <caption className="sr-only">{t('documenteditor.historiquePublications')}</caption>
            <thead>
              <tr><th scope="col">{t('documenteditor.thDate')}</th><th scope="col">{t('documenteditor.cible')}</th><th scope="col">{t('documenteditor.thEtat')}</th><th scope="col">{t('documenteditor.thEmplacement')}</th></tr>
            </thead>
            <tbody>
              {publications.map((p) => (
                <tr key={p.id}>
                  <td>{new Date(p.publieLe).toLocaleString('fr-CA')}</td>
                  <td>{p.cible}</td>
                  <td><span className={`badge s-${p.succes ? 'APPROUVE' : 'REFUSE'}`}>{p.succes ? t('documenteditor.depose') : t('documenteditor.echec')}</span></td>
                  <td>{p.urlCible || '—'}</td>
                </tr>
              ))}
              {publications.length === 0 && <tr><td colSpan="4" className="muted">{t('documenteditor.aucunePublication')}</td></tr>}
            </tbody>
          </table>
        </section>
      </div>

      <div className="panel">
        <div className="addenda-head">
          <h2>{t('documenteditor.addendas')}</h2>
          <button onClick={() => ouvrirModale('addenda')}>{t('documenteditor.nouvelAddenda')}</button>
        </div>
        <ul className="steps">
          {addendas.map((a) => (
            <li key={a.id} className="addenda-item">
              <div className="addenda-ligne">
                <span className="niveau">{t('documenteditor.numeroPrefix', { n: a.numero })}</span>
                <span>{a.objet}</span>
                <span className={`badge s-${a.statut}`}>{a.statut}</span>
                <span className="muted">{t('documenteditor.changementsCount', { n: a.changements?.length || 0 })}</span>
                {a.statut !== 'PUBLIE' && (
                  <span className="btns">
                    <button className="secondary" onClick={() => ouvrirChangement(a.id)}>{t('documenteditor.ajouterChangement')}</button>
                    <button onClick={() => reintegrerAddenda(a.id)}>{t('documenteditor.emettre')}</button>
                  </span>
                )}
              </div>
              {a.changements?.length > 0 && (
                <ul className="addenda-changes">
                  {a.changements.map((ch) => (
                    <li key={ch.id}>
                      <span className="code">{ch.code}</span>
                      <span className="muted"><RichHtml html={ch.nouveauTexte} /></span>
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
          {addendas.length === 0 && <li className="muted">{t('documenteditor.aucunAddenda')}</li>}
        </ul>
      </div>

      <div className="grid2">
        <section className="panel">
          <div className="addenda-head">
            <h2>{t('documenteditor.versionsRedlining')}</h2>
            <button onClick={() => ouvrirModale('version')}>{t('documenteditor.capturerVersion')}</button>
          </div>
          <ul className="steps">
            {versions.map((v) => (
              <li key={v.id}>
                <span className="niveau">v{v.numeroVersion}</span>
                <span>{v.note || '—'}</span>
                <span className="muted">{v.creePar}</span>
              </li>
            ))}
            {versions.length === 0 && <li className="muted">{t('documenteditor.aucuneVersion')}</li>}
          </ul>
          {versions.length >= 2 && (
            <div className="cmp-row">
              <select value={cmp.a} onChange={(e) => setCmp({ ...cmp, a: e.target.value })} aria-label={t('documenteditor.versionA')}>
                <option value="">{t('documenteditor.versionA')}</option>
                {versions.map((v) => <option key={v.id} value={v.id}>v{v.numeroVersion}</option>)}
              </select>
              <select value={cmp.b} onChange={(e) => setCmp({ ...cmp, b: e.target.value })} aria-label={t('documenteditor.versionB')}>
                <option value="">{t('documenteditor.versionB')}</option>
                {versions.map((v) => <option key={v.id} value={v.id}>v{v.numeroVersion}</option>)}
              </select>
              <button className="secondary" onClick={comparer}>{t('documenteditor.comparer')}</button>
            </div>
          )}
          {diff && (
            <div className="diff">
              {diff.map((d) => (
                <div key={d.code} className="diff-clause">
                  <div className="diff-head">
                    <span className="code">{d.code}</span> {d.titre}
                    <span className={`tag chg-${d.changement}`}>{d.changement}</span>
                  </div>
                  {d.changement !== 'INCHANGEE' && d.lignes.map((l, i) => (
                    <div key={i} className={`diff-line dl-${l.type}`}>
                      {l.type === 'AJOUT' ? '+ ' : l.type === 'SUPPRESSION' ? '− ' : '  '}{l.texte}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <div className="addenda-head">
            <h2>{t('documenteditor.obligationsEcheances')}</h2>
            <button onClick={() => ouvrirModale('obligation')}>{t('documenteditor.ajouter')}</button>
          </div>
          <ul className="steps">
            {obligations.map((o) => (
              <li key={o.id}>
                <span>{o.titre}</span>
                <span className="tag">{o.type}</span>
                <span className="muted">{o.dateEcheance || '—'}</span>
                <span className={`badge s-${o.statut}`}>{o.statut}</span>
                {o.statut !== 'COMPLETE' && (
                  <span className="btns"><button className="secondary"
                    onClick={() => completerObligation(o.id)}>{t('documenteditor.completer')}</button></span>
                )}
              </li>
            ))}
            {obligations.length === 0 && <li className="muted">{t('documenteditor.aucuneObligation')}</li>}
          </ul>
        </section>
      </div>

      <div className="panel">
        <div className="addenda-head">
          <h2>{t('documenteditor.signatureElectronique')}</h2>
          <span className="btns">
            <button onClick={() => ouvrirModale('signataire')}>{t('documenteditor.ajouterSignataire')}</button>
            <button className="secondary" onClick={demarrerSignature}
                    disabled={signataires.length === 0}>{t('documenteditor.demarrerSignature')}</button>
          </span>
        </div>
        <ul className="steps">
          {signataires.map((s) => (
            <li key={s.id}>
              <span>{s.nom}</span>
              <span className="muted">{s.role} · {s.courriel}</span>
              <span className={`badge s-${s.statut === 'SIGNE' ? 'APPROUVE' : 'EN_ATTENTE'}`}>{s.statut}</span>
              {s.signatureImage && <img className="sign-thumb" src={s.signatureImage} alt={t('documenteditor.signatureDe', { nom: s.nom })} />}
              {s.statut !== 'SIGNE' && (
                <span className="btns">
                  <button className="secondary" onClick={() => ouvrirSignature(s)}>{t('documenteditor.signer')}</button>
                  {s.jeton && (
                    <button className="secondary" onClick={() => copierLien(s.jeton)}>{t('documenteditor.copierLien')}</button>
                  )}
                </span>
              )}
            </li>
          ))}
          {signataires.length === 0 && <li className="muted">{t('documenteditor.aucunSignataire')}</li>}
        </ul>
      </div>

      <div className="grid2">
        <section className="panel">
          <div className="addenda-head">
            <h2>{t('documenteditor.annexes')}</h2>
            <button onClick={() => ouvrirModale('annexe')}>{t('documenteditor.ajouterAnnexe')}</button>
          </div>
          <ul className="steps">
            {annexes.map((a) => (
              <li key={a.id}>
                <span className="niveau">{a.numero || '—'}</span>
                <span>{a.titre}</span>
                {a.referenceClause && <span className="muted">{t('documenteditor.refPrefix', { ref: a.referenceClause })}</span>}
                <span className="btns">
                  <button className="danger" onClick={() => supprimerAnnexe(a.id)}>{t('documenteditor.supprimer')}</button>
                </span>
              </li>
            ))}
            {annexes.length === 0 && <li className="muted">{t('documenteditor.aucuneAnnexe')}</li>}
          </ul>
        </section>

        <section className="panel">
          <div className="addenda-head">
            <h2>{t('documenteditor.bordereauPrix')}</h2>
            <span className="btns">
              <button className="secondary" onClick={ajouterLigne}>{t('documenteditor.ajouterLigne')}</button>
              <button onClick={enregistrerBordereau}>{t('documenteditor.enregistrer')}</button>
            </span>
          </div>
          <label>{t('documenteditor.titreBordereau')}
            <input value={bordTitre} onChange={(e) => setBordTitre(e.target.value)} />
          </label>
          <table className="bordereau-table">
            <thead>
              <tr><th scope="col">{t('documenteditor.thDescription')}</th><th scope="col">{t('documenteditor.thQuantite')}</th><th scope="col">{t('documenteditor.thTaux')}</th><th scope="col">{t('documenteditor.thTotal')}</th><th scope="col"><span className="sr-only">{t('documenteditor.actions')}</span></th></tr>
            </thead>
            <tbody>
              {bordLignes.map((l) => (
                <tr key={l.cle}>
                  <td><input value={l.description} onChange={(e) => majLigne(l.cle, 'description', e.target.value)} /></td>
                  <td><input type="number" step="0.01" value={l.quantite} onChange={(e) => majLigne(l.cle, 'quantite', e.target.value)} /></td>
                  <td><input type="number" step="0.01" value={l.taux} onChange={(e) => majLigne(l.cle, 'taux', e.target.value)} /></td>
                  <td className="montant">{totalLigne(l).toFixed(2)}</td>
                  <td><button className="danger" onClick={() => retirerLigne(l.cle)} aria-label={t('documenteditor.retirerLigne')}>×</button></td>
                </tr>
              ))}
              {bordLignes.length === 0 && (
                <tr><td colSpan={5} className="muted">{t('documenteditor.aucuneLigne')}</td></tr>
              )}
            </tbody>
            {bordLignes.length > 0 && (
              <tfoot>
                <tr><td colSpan={3} className="montant"><strong>{t('documenteditor.total')}</strong></td>
                  <td className="montant"><strong>{totalBordereau().toFixed(2)}</strong></td><td></td></tr>
              </tfoot>
            )}
          </table>
        </section>
      </div>

      <div className="panel">
        <AiAssistant
          contexteParDefaut={`${doc.type} — ${doc.contexte || ''}`}
          onInsertion={(texte) => copier(texte, t('documenteditor.iaCopie'))}
        />
        <p className="muted">{t('documenteditor.iaCopie')}</p>
      </div>

      {modal && (
        <PromptModal
          titre={MODALES[modal.kind].titre}
          champs={modal.kind === 'obligation'
            ? [{ name: 'type', label: t('documenteditor.type'), type: 'select', options: typesObligation }, ...MODALES.obligation.champs]
            : MODALES[modal.kind].champs}
          valeurs={mForm}
          setValeurs={setMForm}
          onValider={soumettreModale}
          onClose={() => setModal(null)}
        />
      )}

      {apercu && (
        <Modal titre={t('documenteditor.apercuContratTitre')} onClose={fermerApercu} taille="lg">
          <div className="pdf-apercu-cadre">
            <img className="pdf-apercu" src={apercu.url} alt={t('documenteditor.apercuAlt')} />
          </div>
          <p className="muted">{t('documenteditor.apercuAide')}</p>
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={fermerApercu}>{t('documenteditor.fermer')}</button>
            <button type="button" onClick={() => { exporter('pdf') }}>{t('documenteditor.telechargerPdf')}</button>
          </div>
        </Modal>
      )}

      {changeAddenda && (
        <Modal titre={t('documenteditor.modifierClauseAddenda')} onClose={() => setChangeAddenda(null)} taille="lg">
          <form className="form" onSubmit={(e) => { e.preventDefault(); enregistrerChangement() }}>
            <label>{t('documenteditor.clauseAModifier')}
              <select value={changeAddenda.clauseId}
                      onChange={(e) => setChangeAddenda({ ...changeAddenda, clauseId: e.target.value })} required>
                <option value="">{t('documenteditor.choisirClause')}</option>
                {(doc.clauses || []).filter((dc) => dc.clauseId).map((dc) => (
                  <option key={dc.id} value={dc.clauseId}>{dc.numero ? `${dc.numero} ` : ''}{dc.code} — {dc.titre}</option>
                ))}
              </select>
            </label>
            <label>{t('documenteditor.nouveauTexteClause')}
              <RichText value={changeAddenda.nouveauTexte} libelle={t('documenteditor.nouveauTexteClause')}
                        onChange={(v) => setChangeAddenda({ ...changeAddenda, nouveauTexte: v })}
                        rows={8} placeholder={t('documenteditor.texteAddendaPlaceholder')} />
            </label>
            <p className="muted">{t('documenteditor.changementAide')}</p>
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setChangeAddenda(null)}>{t('documenteditor.annuler')}</button>
              <button type="submit">{t('documenteditor.ajouterLeChangement')}</button>
            </div>
          </form>
        </Modal>
      )}

      {clauseEdit && (
        <Modal titre={clauseEdit.id ? t('documenteditor.redigerSection') : t('documenteditor.nouvelleSectionLibre')}
               onClose={() => setClauseEdit(null)} taille="lg">
          <form className="form" onSubmit={(e) => { e.preventDefault(); enregistrerRedaction() }}>
            <div className="form-grid">
              <label>{t('documenteditor.titreSection')}
                <input value={clauseEdit.titre} onChange={(e) => setClauseEdit({ ...clauseEdit, titre: e.target.value })}
                       placeholder={t('documenteditor.titreSectionPlaceholder')} />
              </label>
              <label>{t('documenteditor.regroupement')}
                <input value={clauseEdit.section} onChange={(e) => setClauseEdit({ ...clauseEdit, section: e.target.value })}
                       placeholder={t('documenteditor.regroupementPlaceholder')} />
              </label>
            </div>
            <label>{t('documenteditor.contenu')}
              <RichText value={clauseEdit.texteEffectif} libelle={t('documenteditor.contenu')}
                        onChange={(v) => setClauseEdit({ ...clauseEdit, texteEffectif: v })}
                        rows={10} placeholder={t('documenteditor.contenuPlaceholder')} />
            </label>
            <p className="muted">{t('documenteditor.redactionAide')}</p>
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setClauseEdit(null)}>{t('documenteditor.annuler')}</button>
              <button type="submit">{clauseEdit.id ? t('documenteditor.enregistrer') : t('documenteditor.ajouterSection')}</button>
            </div>
          </form>
        </Modal>
      )}

      {signature && (
        <Modal titre={t('documenteditor.signatureTitre', { nom: signature.signataire.nom })} onClose={() => setSignature(null)}>
          <form className="form" onSubmit={(e) => { e.preventDefault(); signerManuel() }}>
            <label>{t('documenteditor.nomCompletSignataire')}
              <input value={signNom} required onChange={(e) => setSignNom(e.target.value)} />
            </label>
            <div className="form-legend">{t('documenteditor.signatureManuscrite')}</div>
            <SignaturePad onChange={setSignImg} />
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setSignature(null)}>{t('documenteditor.annuler')}</button>
              <button type="submit">{t('documenteditor.apposerSignature')}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
