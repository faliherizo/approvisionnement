import { useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'
import { slugTenant } from '../theme.js'
import Modal from './Modal.jsx'

/** Route générique d'un écran : un identifiant numérique devient « :id » (/clm/trousses/42 → /clm/trousses/:id). */
const generique = (chemin) => chemin.replace(/\/\d+(?=\/|$)/g, '/:id').replace(/\/$/, '') || '/'
const minutes = (s) => (s >= 60 ? `${Math.floor(s / 60)} min ${String(s % 60).padStart(2, '0')} s` : `${s} s`)

function Tutoriel({ tuto, onRetour }) {
  const { t } = useI18n()
  return (
    <article className="aide-tutoriel" aria-labelledby="aide-tuto-titre">
      <button type="button" className="secondary" onClick={onRetour}>{t('aide.retour')}</button>
      <h4 id="aide-tuto-titre">{tuto.titre}</h4>
      <p className="muted">{t('aide.duree', { duree: minutes(tuto.duree) })}{tuto.exigences?.length ? ` · ${t('aide.exigences', { liste: tuto.exigences.join(', ') })}` : ''}</p>
      <video className="aide-video" controls preload="metadata" playsInline aria-describedby="aide-tuto-etapes">
        <source src={`/api/media/${tuto.video}`} type="video/mp4" />
        {tuto.sousTitres && <track kind="captions" srcLang="fr" label="Français" src={`/api/media/${tuto.sousTitres}`} default />}
        {t('aide.videoIndisponible')}
      </video>
      <h5>{t('aide.etapes')}</h5>
      <ol id="aide-tuto-etapes" className="aide-etapes">
        {tuto.etapes.map((e, i) => <li key={i}>{e}</li>)}
      </ol>
    </article>
  )
}

function Liste({ titre, tutos, onChoisir, vide }) {
  const { t } = useI18n()
  return (
    <section className="aide-section" aria-label={titre}>
      <h4>{titre}</h4>
      {tutos.length === 0 ? <p className="muted">{vide}</p> : (
        <ul className="aide-liste">
          {tutos.map((x) => (
            <li key={x.id}>
              <button type="button" className="lien" onClick={() => onChoisir(x)}>{x.titre}</button>
              <span className="muted"> · {minutes(x.duree)}{x.profil ? ` · ${t('aide.profil', { profil: x.profil })}` : ''}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

/**
 * Menu d'aide : tutoriels vidéo narrés et sous-titrés, tournés à partir des scénarios e2e de chaque exigence ; ceux de
 * l'écran courant d'abord, la mise en route de l'organisation pour son administration, puis tous les autres.
 */
export default function AideMenu({ espace }) {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { pathname } = useLocation()
  const [ouvert, setOuvert] = useState(false)
  const [index, setIndex] = useState(null)
  const [erreur, setErreur] = useState(false)
  const [choisi, setChoisi] = useState(null)
  const [q, setQ] = useState('')
  const organisation = slugTenant()

  useEffect(() => {
    if (!ouvert || index) return
    api.get('/media/aide/index.json').then((r) => setIndex(Array.isArray(r.data) ? r.data : [])).catch(() => { setIndex([]); setErreur(true) })
  }, [ouvert, index])

  // Les tutoriels de l'organisation courante ; à défaut, ceux de Lévis, tournés sur les mêmes écrans du socle.
  const tutos = useMemo(() => {
    const espaceCourant = (index || []).filter((x) => x.espace === espace)
    const propres = espaceCourant.filter((x) => x.organisation === organisation)
    if (propres.length) return propres
    return espace === 'clm' ? espaceCourant : espaceCourant.filter((x) => x.organisation === 'levis')
  }, [index, espace, organisation])
  const ici = generique(pathname)
  const admin = hasRole('ADMIN') || hasRole('ADMIN_AFFAIRES') || hasRole('ADMIN_TECHNIQUE')
  const miseEnRoute = tutos.filter((x) => x.assemblage)
  const ecran = tutos.filter((x) => !x.assemblage && x.routes?.includes(ici))
  const recherche = q.trim().toLowerCase()
  const tous = tutos.filter((x) => !x.assemblage && (!recherche || `${x.titre} ${(x.exigences || []).join(' ')}`.toLowerCase().includes(recherche)))
  const fermer = () => { setOuvert(false); setChoisi(null) }

  return (
    <>
      <button type="button" className="aide-bouton" aria-haspopup="dialog" onClick={() => setOuvert(true)}><span aria-hidden="true">❔</span> {t('aide.bouton')}</button>
      {ouvert && (
        <Modal titre={t('aide.titre')} onClose={fermer} taille="lg">
          {choisi ? <Tutoriel tuto={choisi} onRetour={() => setChoisi(null)} /> : (
            <div className="aide-contenu">
              {index === null && <p role="status">{t('aide.chargement')}</p>}
              {index !== null && tutos.length === 0 && <p className="muted" role={erreur ? 'alert' : undefined}>{t('aide.aucun')}</p>}
              {admin && miseEnRoute.length > 0 && (
                <Liste titre={t('aide.miseEnRoute')} tutos={miseEnRoute} onChoisir={setChoisi} vide="" />
              )}
              {tutos.length > 0 && <Liste titre={t('aide.cetEcran')} tutos={ecran} onChoisir={setChoisi} vide={t('aide.aucunEcran')} />}
              {tutos.length > 0 && (
                <>
                  <label>{t('aide.rechercher')}<input type="search" value={q} onChange={(e) => setQ(e.target.value)} /></label>
                  <Liste titre={t('aide.tous', { n: tous.length })} tutos={tous} onChoisir={setChoisi} vide={t('aide.aucunResultat')} />
                </>
              )}
            </div>
          )}
        </Modal>
      )}
    </>
  )
}
