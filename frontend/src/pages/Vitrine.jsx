import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api.js'
import { useI18n } from '../i18n/I18nContext.jsx'
import ChoixOrganisation from '../components/ChoixOrganisation.jsx'
import { slugTenant } from '../theme.js'

/**
 * Accès à l'application : sur le sous-domaine d'une organisation, lien direct vers sa connexion ; sur le domaine de la
 * plateforme, ouverture du choix de l'organisation.
 */
function Acces({ className, children, onChoisir }) {
  if (slugTenant()) return <Link className={className} to="/login">{children}</Link>
  return <button type="button" className={className} aria-haspopup="dialog" onClick={onChoisir}>{children}</button>
}

/**
 * Site vitrine public (hors authentification) : présentation de la solution, vidéo de
 * démonstration et appels à l'action. Responsive et conforme WCAG 2.2 AA (landmarks,
 * ordre des titres, cibles ≥ 24 px, focus visible, alternative textuelle à la vidéo).
 */
/**
 * Vitrine d'une organisation (https://<organisation>.contrat.vinaitsolution.com/accueil) : son titre, ses fonctionnalités
 * et sa vidéo de présentation sous-titrée, tels que le superadministrateur les a publiés.
 */
function VitrineOrganisation({ v }) {
  const { t } = useI18n()
  return (
    <div className="vitrine">
      <a className="skip-link" href="#contenu">{t('vitrine.skipLink')}</a>
      <header className="vitrine-topbar">
        <div className="vitrine-brand">
          <span className="crest" aria-hidden="true">⚜️</span>
          <div><strong>{v.titre}</strong><span className="vitrine-sub">{t('vitrine.orgSous')}</span></div>
        </div>
        <nav aria-label={t('vitrine.navLabel')} className="vitrine-nav">
          {v.video && <a href="#demonstration">{t('vitrine.navDemo')}</a>}
          <a href="#fonctionnalites">{t('vitrine.navFeatures')}</a>
          <Link className="vitrine-cta" to="/login">{t('vitrine.orgConnexion')}</Link>
        </nav>
      </header>
      <main id="contenu">
        <section className="vitrine-hero" aria-labelledby="titre-hero">
          <h1 id="titre-hero">{v.titre}</h1>
          {v.accroche && <p className="vitrine-accroche">{v.accroche}</p>}
          <div className="vitrine-actions">
            <Link className="vitrine-bouton" to="/login">{t('vitrine.orgConnexion')}</Link>
            {v.video && <a className="vitrine-bouton secondaire" href="#demonstration">{t('vitrine.heroSeeVideo')}</a>}
          </div>
        </section>
        {v.video && (
          <section id="demonstration" className="vitrine-section" aria-labelledby="titre-demo">
            <h2 id="titre-demo">{t('vitrine.orgVideo')}</h2>
            <figure className="vitrine-video">
              <video controls preload="metadata" playsInline width="1280" height="720" aria-describedby="video-org-alt"
                poster={v.affiche ? `/api/media/${v.affiche}` : undefined}>
                <source src={`/api/media/${v.video}`} type={v.video.endsWith('.webm') ? 'video/webm' : 'video/mp4'} />
                {v.sousTitres && <track kind="captions" srcLang="fr" label="Français" src={`/api/media/${v.sousTitres}`} default />}
                {t('vitrine.videoFallback')}
              </video>
              <figcaption id="video-org-alt">{t('vitrine.orgVideoAlt')}</figcaption>
            </figure>
          </section>
        )}
        {v.fonctionnalites.length > 0 && (
          <section id="fonctionnalites" className="vitrine-section" aria-labelledby="titre-fonc">
            <h2 id="titre-fonc">{t('vitrine.featuresTitle')}</h2>
            <ul className="vitrine-grille" role="list">
              {v.fonctionnalites.map((f) => (
                <li key={f.titre} className="vitrine-carte"><h3>{f.titre}</h3>{f.texte && <p>{f.texte}</p>}</li>
              ))}
            </ul>
          </section>
        )}
      </main>
      <footer className="vitrine-footer">
        <p>{t('vitrine.orgPropulse')} <a href="https://vinaitsolution.com">VINA IT Solutions inc.</a></p>
      </footer>
    </div>
  )
}

export default function Vitrine() {
  const { t } = useI18n()
  const [choixOuvert, setChoixOuvert] = useState(false)
  const choisir = () => setChoixOuvert(true)
  // Sur le sous-domaine d'une organisation : sa vitrine publiée, sinon celle de la plateforme.
  const organisation = slugTenant()
  const [vitrine, setVitrine] = useState(organisation ? undefined : null)
  useEffect(() => {
    if (!organisation) return
    api.get(`/vitrines/${organisation}`).then((r) => setVitrine(r.data)).catch(() => setVitrine(null))
  }, [organisation])
  if (vitrine === undefined) return <p className="vitrine-chargement" role="status">{t('vitrine.chargement')}</p>
  if (vitrine) return <VitrineOrganisation v={vitrine} />

  const fonctionnalites = [
    { titre: t('vitrine.feat1Title'), texte: t('vitrine.feat1Text') },
    { titre: t('vitrine.feat2Title'), texte: t('vitrine.feat2Text') },
    { titre: t('vitrine.feat3Title'), texte: t('vitrine.feat3Text') },
    { titre: t('vitrine.feat4Title'), texte: t('vitrine.feat4Text') },
    { titre: t('vitrine.feat5Title'), texte: t('vitrine.feat5Text') },
    { titre: t('vitrine.feat6Title'), texte: t('vitrine.feat6Text') },
    { titre: t('vitrine.feat7Title'), texte: t('vitrine.feat7Text') },
    { titre: t('vitrine.feat8Title'), texte: t('vitrine.feat8Text') },
  ]

  return (
    <div className="vitrine">
      <a className="skip-link" href="#contenu">{t('vitrine.skipLink')}</a>

      <header className="vitrine-topbar">
        <div className="vitrine-brand">
          <span className="crest" aria-hidden="true">⚜️</span>
          <div>
            <strong>VINA IT Solutions inc.</strong>
            <span className="vitrine-sub">{t('vitrine.brandSub')}</span>
          </div>
        </div>
        <nav aria-label={t('vitrine.navLabel')} className="vitrine-nav">
          <a href="#demonstration">{t('vitrine.navDemo')}</a>
          <a href="#fonctionnalites">{t('vitrine.navFeatures')}</a>
          <a href="#atouts">{t('vitrine.navAtouts')}</a>
          <Acces className="vitrine-cta" onChoisir={choisir}>{t('vitrine.navAccess')}</Acces>
        </nav>
      </header>

      <main id="contenu">
        <section className="vitrine-hero" aria-labelledby="titre-hero">
          <h1 id="titre-hero">{t('vitrine.heroTitle')}</h1>
          <p className="vitrine-accroche">
            {t('vitrine.heroAccrocheA')}<strong>{t('vitrine.heroAccrocheStrong')}</strong>{t('vitrine.heroAccrocheB')}
          </p>
          <div className="vitrine-actions">
            <Acces className="vitrine-bouton" onChoisir={choisir}>{t('vitrine.heroTryDemo')}</Acces>
            <a className="vitrine-bouton secondaire" href="#demonstration">{t('vitrine.heroSeeVideo')}</a>
          </div>
        </section>

        <section id="demonstration" className="vitrine-section" aria-labelledby="titre-demo">
          <h2 id="titre-demo">{t('vitrine.demoTitle')}</h2>
          <p>
            {t('vitrine.demoParaA')}<strong>{t('vitrine.demoParaStrong1')}</strong>{t('vitrine.demoParaMid')}<strong>{t('vitrine.demoParaStrong2')}</strong>{t('vitrine.demoParaB')}
          </p>
          <figure className="vitrine-video">
            <video controls preload="metadata" playsInline poster="/media/demo-vina-poster.jpg"
                   aria-describedby="video-alt" width="1280" height="800">
              <source src="/media/demo-vina.mp4" type="video/mp4" />
              <source src="/media/demo-vina.webm" type="video/webm" />
              {t('vitrine.videoFallback')}
              <a href="/login">{t('vitrine.videoFallbackLink')}</a>.
            </video>
            <figcaption id="video-alt">
              {t('vitrine.videoCaption')}
            </figcaption>
          </figure>
        </section>

        <section id="fonctionnalites" className="vitrine-section" aria-labelledby="titre-fonc">
          <h2 id="titre-fonc">{t('vitrine.featuresTitle')}</h2>
          <ul className="vitrine-grille" role="list">
            {fonctionnalites.map((f) => (
              <li key={f.titre} className="vitrine-carte">
                <h3>{f.titre}</h3>
                <p>{f.texte}</p>
              </li>
            ))}
          </ul>
        </section>

        <section id="atouts" className="vitrine-section vitrine-atouts" aria-labelledby="titre-atouts">
          <h2 id="titre-atouts">{t('vitrine.atoutsTitle')}</h2>
          <div className="vitrine-grille">
            <div className="vitrine-carte">
              <h3>{t('vitrine.atout1Title')}</h3>
              <p>{t('vitrine.atout1Text')}</p>
            </div>
            <div className="vitrine-carte">
              <h3>{t('vitrine.atout2Title')}</h3>
              <p>{t('vitrine.atout2Text')}</p>
            </div>
            <div className="vitrine-carte">
              <h3>{t('vitrine.atout3Title')}</h3>
              <p>{t('vitrine.atout3Text')}</p>
            </div>
            <div className="vitrine-carte">
              <h3>{t('vitrine.atout4Title')}</h3>
              <p>{t('vitrine.atout4Text')}</p>
            </div>
          </div>
          <p className="vitrine-refs">
            {t('vitrine.refs')}
          </p>
        </section>

        <section className="vitrine-section vitrine-final" aria-labelledby="titre-contact">
          <h2 id="titre-contact">{t('vitrine.finalTitle')}</h2>
          <p>{t('vitrine.finalText')}</p>
          <div className="vitrine-actions">
            <Acces className="vitrine-bouton" onChoisir={choisir}>{t('vitrine.finalAccess')}</Acces>
            <a className="vitrine-bouton secondaire" href="mailto:faliherizo@vinaitsolution.com">{t('vitrine.finalContact')}</a>
          </div>
        </section>
      </main>

      <footer className="vitrine-footer">
        <p>© {new Date().getFullYear()} VINA IT Solutions inc. — <a href="https://vinaitsolution.com">vinaitsolution.com</a></p>
        <p className="vitrine-sub">{t('vitrine.footerTagline')}</p>
      </footer>
      {choixOuvert && <ChoixOrganisation onClose={() => setChoixOuvert(false)} />}
    </div>
  )
}
