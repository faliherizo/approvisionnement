package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.IdentiteOrganisation;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Portail du signataire (ST-09, RST-17, RST-19, UI-16), accessible sans compte par un jeton à usage unique.
 * Ordre imposé : vérification d'identité, lecture, consentement explicite, signature horodatée, reçu.
 */
@Service
public class PortailService {

    private static final String INTROUVABLE = "Invitation introuvable ou lien invalide.";

    private final InvitationRepository invitations;
    private final ParticipantRepository participants;
    private final DocumentTrousseRepository documents;
    private final FournisseurSignature fournisseur;
    private final ParcoursService parcours;
    private final TrousseService trousses;
    private final MessagerieClm messagerie;
    private final SourceAleatoire alea;
    private final LectureTrousse lecture;
    private final JournalClm journal;
    private final ContexteClm ctx;
    private final IdentiteOrganisation identite;

    public PortailService(InvitationRepository invitations, ParticipantRepository participants, DocumentTrousseRepository documents,
                          FournisseurSignature fournisseur, ParcoursService parcours, TrousseService trousses,
                          MessagerieClm messagerie, SourceAleatoire alea, LectureTrousse lecture, JournalClm journal, ContexteClm ctx,
                          IdentiteOrganisation identite) {
        this.invitations = invitations;
        this.participants = participants;
        this.documents = documents;
        this.fournisseur = fournisseur;
        this.parcours = parcours;
        this.trousses = trousses;
        this.messagerie = messagerie;
        this.alea = alea;
        this.lecture = lecture;
        this.journal = journal;
        this.ctx = ctx;
        this.identite = identite;
    }

    private record Session(Invitation invitation, Participant participant, Trousse trousse) {
    }

    @Transactional(readOnly = true)
    public ClmDto.PortailVue consulter(String jeton) {
        return dansLeTenant(jeton, s -> vue(s));
    }

    @Transactional
    public ClmDto.PortailVue demarrerVerification(String jeton) {
        return dansLeTenant(jeton, s -> {
            Invitation inv = s.invitation();
            inv.exigerOuverte(ctx.maintenant());
            if (inv.getStatut() == Invitation.Statut.VERIFIEE) {
                return vue(s);
            }
            List<String> canaux = Identite.canauxDuCode(inv.getNiveau());
            if (!canaux.isEmpty()) {
                String code = alea.code();
                inv.preparerCode(hash(code), ctx.maintenant().plus(Duration.ofMinutes(Identite.CODE_VALIDITE_MINUTES)));
                envoyerCode(s, code, canaux);
                invitations.save(inv);
            }
            journal.consigner(s.trousse(), "VERIFICATION_DEMARREE", s.participant().getCourriel(), inv.getNiveau().name());
            return vue(s);
        });
    }

    /** Vérification forte : chaque canal reçoit une moitié du code, il faut les deux pour l'identifier. */
    private void envoyerCode(Session s, String code, List<String> canaux) {
        int moitie = code.length() / 2;
        for (int i = 0; i < canaux.size(); i++) {
            String partie = canaux.size() == 1 ? code : (i == 0 ? code.substring(0, moitie) : code.substring(moitie));
            String canal = canaux.get(i);
            String destinataire = "SMS".equals(canal) ? s.participant().getTelephone() : s.participant().getCourriel();
            Map<String, Object> vars = new HashMap<>(Map.of("reference", s.trousse().getReference(), "code", partie));
            messagerie.envoyer(s.trousse().getTenantId(), Evenement.CODE, canal, destinataire, vars, List.of());
        }
    }

    /** Un échec consomme une tentative et doit survivre au refus : il n'annule pas la transaction. */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public ClmDto.PortailVue verifier(String jeton, ClmDto.VerificationRequest req) {
        return dansLeTenant(jeton, s -> {
            Invitation inv = s.invitation();
            Instant maintenant = ctx.maintenant();
            inv.exigerOuverte(maintenant);
            String reponse = req == null || req.reponse() == null ? "" : req.reponse();
            if (!reponseValide(s, reponse, maintenant)) {
                inv.echecVerification();
                invitations.save(inv);
                journal.consigner(s.trousse(), "VERIFICATION_ECHOUEE", s.participant().getCourriel(), "tentative " + inv.getTentatives());
                if (inv.getStatut() == Invitation.Statut.BLOQUEE) {
                    throw new IllegalArgumentException("Vérification échouée : invitation bloquée, contactez l'expéditeur.");
                }
                throw new IllegalArgumentException("Vérification échouée : " + (Identite.TENTATIVES_MAX - inv.getTentatives()) + " tentative(s) restante(s).");
            }
            inv.verifier(maintenant);
            invitations.save(inv);
            journal.consigner(s.trousse(), "IDENTITE_VERIFIEE", s.participant().getCourriel(), inv.getNiveau().name());
            return vue(s);
        });
    }

    private boolean reponseValide(Session s, String reponse, Instant maintenant) {
        Invitation inv = s.invitation();
        if (inv.getNiveau() == Identite.Niveau.QUESTION) {
            String attendu = s.participant().getReponseIdentiteHash();
            return attendu != null && attendu.equals(hash(Identite.normaliserReponse(reponse)));
        }
        return inv.getCodeHash() != null && inv.getCodeExpireLe() != null && !maintenant.isAfter(inv.getCodeExpireLe())
                && inv.getCodeHash().equals(hash(reponse.replaceAll("\\s", "")));
    }

    @Transactional
    public DocumentTrousse document(String jeton, Long documentId) {
        return dansLeTenant(jeton, s -> {
            exigerVerifiee(s);
            DocumentTrousse d = documentsASigner(s.trousse()).stream().filter(x -> x.getId().equals(documentId)).findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Document retiré ou inaccessible."));
            journal.consigner(s.trousse(), "DOCUMENT_CONSULTE", s.participant().getCourriel(), d.getNom() + " v" + d.getVersion());
            return d;
        });
    }

    @Transactional
    public ClmDto.Recu signer(String jeton, ClmDto.SignatureRequest req, String ip, String agent) {
        return dansLeTenant(jeton, s -> {
            Invitation inv = s.invitation();
            Instant maintenant = ctx.maintenant();
            exigerVerifiee(s);
            if (req == null || !req.consentement()) {
                throw new IllegalArgumentException("Consentement explicite requis avant de signer.");
            }
            String nom = req.nomComplet() == null ? "" : req.nomComplet().trim();
            if (nom.length() < 2) {
                throw new IllegalArgumentException("Nom complet requis pour signer.");
            }
            Map<String, String> champs = validerChamps(s, req.champs() == null ? Map.of() : req.champs());
            inv.consentir(maintenant);
            journal.consigner(s.trousse(), "CONSENTEMENT", s.participant().getCourriel(), "Consentement à signer électroniquement");
            String empreinte = empreinteDocuments(s.trousse());
            FournisseurSignature.Attestation att = fournisseur.signer(new FournisseurSignature.Demande(empreinte, nom,
                    s.participant().getCourriel(), inv.getNiveau(), maintenant, ip));
            Map<String, Object> attestation = attestation(att, empreinte);
            inv.signer(nom, tronquer(ip, 64), tronquer(agent, 300), ctx.ecrire(champs), ctx.ecrire(attestation), maintenant);
            invitations.save(inv);
            journal.consigner(s.trousse(), "SIGNATURE", s.participant().getCourriel(), nom + " · " + inv.getNiveau()
                    + " · " + empreinte.substring(0, 16) + " · " + att.identifiant());
            EtapeTrousse e = parcours.exigerEtape(inv.getEtapeId());
            parcours.enregistrerSignature(s.trousse(), e, s.participant(), true, null);
            return new ClmDto.Recu(s.trousse().getReference(), nom, maintenant, empreinte, att.identifiant());
        });
    }

    @Transactional
    public ClmDto.PortailVue refuser(String jeton, ClmDto.RefusRequest req, String ip) {
        return dansLeTenant(jeton, s -> {
            Invitation inv = s.invitation();
            inv.exigerOuverte(ctx.maintenant());
            String motif = req == null || req.motif() == null || req.motif().isBlank() ? null : req.motif().trim();
            if (motif == null) {
                throw new IllegalArgumentException("Motif du refus ou du problème requis.");
            }
            inv.refuser(tronquer(motif, 1000), tronquer(ip, 64), ctx.maintenant());
            invitations.save(inv);
            journal.consigner(s.trousse(), "SIGNATURE_REFUSEE", s.participant().getCourriel(), motif);
            parcours.enregistrerSignature(s.trousse(), parcours.exigerEtape(inv.getEtapeId()), s.participant(), false, motif);
            return vue(s);
        });
    }

    // --- interne ---
    private <T> T dansLeTenant(String jeton, java.util.function.Function<Session, T> action) {
        if (jeton == null || jeton.length() < 20 || jeton.length() > 200) {
            throw new NoSuchElementException(INTROUVABLE);
        }
        Invitation inv = invitations.findByJetonHash(hash(jeton)).orElseThrow(() -> new NoSuchElementException(INTROUVABLE));
        return ctx.dansTenant(inv.getTenantId(), () -> {
            Trousse t = lecture.exiger(inv.getTrousseId());
            Participant p = participants.findByIdAndTenantId(inv.getParticipantId(), inv.getTenantId())
                    .orElseThrow(() -> new NoSuchElementException(INTROUVABLE));
            return action.apply(new Session(inv, p, t));
        });
    }

    private void exigerVerifiee(Session s) {
        s.invitation().exigerOuverte(ctx.maintenant());
        if (s.trousse().getStatut() != StatutTrousse.EN_TRAITEMENT) {
            throw new IllegalStateException("Document retiré : la trousse n'est plus en signature.");
        }
        if (s.invitation().getStatut() != Invitation.Statut.VERIFIEE) {
            throw new IllegalStateException("Identité à vérifier.");
        }
    }

    private List<DocumentTrousse> documentsASigner(Trousse t) {
        return documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId()).stream()
                .filter(d -> d.getStatut() == DocumentTrousse.Statut.ENVOYE).toList();
    }

    /** Empreinte de l'ensemble signé : chaque document y entre par son empreinte, dans un ordre stable. */
    String empreinteDocuments(Trousse t) {
        StringBuilder sb = new StringBuilder();
        documentsASigner(t).forEach(d -> sb.append(d.getCleDocument()).append(':').append(d.getEmpreinte()).append('\n'));
        return ChaineScellee.sha256(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<Modele.Champ> champsAssignes(Session s) {
        String role = s.participant().getRoleContrat();
        return trousses.champsDuModele(s.trousse()).stream()
                .filter(c -> c.type() != Modele.TypeChamp.SIGNATURE && role.equals(c.roleDestinataire())).toList();
    }

    private Map<String, String> validerChamps(Session s, Map<String, String> saisis) {
        Map<String, String> retenus = new HashMap<>();
        List<String> erreurs = new ArrayList<>();
        for (Modele.Champ c : champsAssignes(s)) {
            String v = saisis.get(c.cle());
            boolean vide = v == null || v.isBlank();
            if (vide && c.obligatoire()) {
                erreurs.add("Champ obligatoire : " + c.libelle());
            } else if (!vide && c.format() != null && !c.format().isBlank() && !Pattern.compile(c.format()).matcher(v).matches()) {
                erreurs.add("Format invalide : " + c.libelle());
            } else if (!vide && c.type() == Modele.TypeChamp.LISTE && !c.options().contains(v)) {
                erreurs.add("Valeur non permise : " + c.libelle());
            } else if (!vide) {
                retenus.put(c.cle(), v.trim());
            }
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        return retenus;
    }

    private static Map<String, Object> attestation(FournisseurSignature.Attestation a, String empreinte) {
        Map<String, Object> m = new HashMap<>();
        m.put("fournisseur", a.fournisseur());
        m.put("identifiant", a.identifiant());
        m.put("horodatage", a.horodatage().toString());
        m.put("jetonHorodatage", a.jetonHorodatage());
        m.put("algorithme", a.algorithme());
        m.put("empreinteDocuments", empreinte);
        return m;
    }

    private ClmDto.PortailVue vue(Session s) {
        Invitation inv = s.invitation();
        boolean verifiee = inv.getStatut() == Invitation.Statut.VERIFIEE || inv.getStatut() == Invitation.Statut.SIGNEE;
        List<ClmDto.DocumentPortail> docs = inv.getStatut() == Invitation.Statut.VERIFIEE
                ? documentsASigner(s.trousse()).stream().map(d -> new ClmDto.DocumentPortail(d.getId(), d.getNom(), d.getType().name(),
                d.getTaille(), d.getEmpreinte())).toList()
                : List.of();
        List<ClmDto.ChampPortail> champs = verifiee ? champsAssignes(s).stream().map(c -> new ClmDto.ChampPortail(c.cle(), c.libelle(),
                c.type().name(), c.obligatoire(), c.options(), c.position())).toList() : List.of();
        ClmDto.Recu recu = null;
        if (inv.getStatut() == Invitation.Statut.SIGNEE) {
            Map<String, Object> att = ctx.lireObjet(inv.getAttestationJson());
            recu = new ClmDto.Recu(s.trousse().getReference(), inv.getNomSigne(), inv.getSigneeLe(),
                    String.valueOf(att.get("empreinteDocuments")), String.valueOf(att.get("identifiant")));
        }
        String question = inv.getNiveau() == Identite.Niveau.QUESTION ? s.participant().getQuestionIdentite() : null;
        String statut = inv.estExpiree(ctx.maintenant()) && inv.getStatut() != Invitation.Statut.SIGNEE ? "EXPIREE" : inv.getStatut().name();
        IdentiteOrganisation.Organisation organisation = identite.de(s.trousse().getTenantId());
        return new ClmDto.PortailVue(s.trousse().getReference(), s.trousse().getTitre(), s.participant().getNom(), inv.getNiveau().name(),
                statut, inv.getExpireLe(), verifiee, inv.getConsentementLe() != null, question, docs, champs, recu,
                organisation.nom(), organisation.logo());
    }

    private static String hash(String s) {
        return ChaineScellee.sha256(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String tronquer(String s, int max) {
        return s == null ? null : s.substring(0, Math.min(s.length(), max));
    }
}
