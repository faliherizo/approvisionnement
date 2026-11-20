package qc.levis.contrat.clm.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.CachetElectronique;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Dossier de preuve (ST-10, UI-19) : documents signés et sources, identités, consentements, horodatages et journal
 * scellé, exportés dans une archive ZIP autoportante (formats ouverts JSON/PDF, NF-13) et scellée par HMAC.
 */
@Service
public class PreuveService {

    private final DocumentTrousseRepository documents;
    private final InvitationRepository invitations;
    private final ParticipantRepository participants;
    private final EvenementTrousseRepository evenements;
    private final FournisseurSignature fournisseur;
    private final Scelleur scelleur;
    private final RenduDocuments rendu;
    private final LectureTrousse lecture;
    private final JournalClm journal;
    private final ContexteClm ctx;
    private final AlertesExploitation alertes;
    private final CachetElectronique cachet;

    public PreuveService(DocumentTrousseRepository documents, InvitationRepository invitations, ParticipantRepository participants,
                         EvenementTrousseRepository evenements, FournisseurSignature fournisseur, Scelleur scelleur,
                         RenduDocuments rendu, LectureTrousse lecture, JournalClm journal, ContexteClm ctx, AlertesExploitation alertes,
                         CachetElectronique cachet) {
        this.documents = documents;
        this.invitations = invitations;
        this.participants = participants;
        this.evenements = evenements;
        this.fournisseur = fournisseur;
        this.scelleur = scelleur;
        this.rendu = rendu;
        this.lecture = lecture;
        this.journal = journal;
        this.ctx = ctx;
        this.alertes = alertes;
        this.cachet = cachet;
    }

    public ClmDto.PreuveVue dossier(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        Instant maintenant = ctx.maintenant();
        List<ClmDto.DocumentVue> finaux = documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())
                .stream().filter(d -> d.getStatut() == DocumentTrousse.Statut.SIGNE || d.getStatut() == DocumentTrousse.Statut.ENVOYE)
                .map(LectureTrousse::document).toList();
        return new ClmDto.PreuveVue(lecture.resume(t, List.of(), maintenant), finaux, signatures(t),
                evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId()).stream()
                        .map(LectureTrousse::evenement).toList(), controler(t));
    }

    @Transactional
    public ClmDto.Integrite verifier(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        ClmDto.Integrite i = controler(t);
        journal.consigner(t, "INTEGRITE_VERIFIEE", acteur.id(), i.integre() ? "intègre" : "ALTÉRATION DÉTECTÉE");
        return i;
    }

    /** Une altération constatée, quelle que soit la consultation qui la révèle, est signalée à l'exploitation (ST-14). */
    private ClmDto.Integrite controler(Trousse t) {
        ClmDto.Integrite i = integrite(t);
        if (!i.integre()) {
            alertes.signaler(AlertesExploitation.Code.INTEGRITE_ALTEREE, t.getTenantId(), t.getReference(),
                    "journal " + (i.journalIntegre() ? "intact" : "rompu") + " · " + i.documentsAlteres().size() + " document(s) altéré(s) · "
                            + i.attestationsInvalides().size() + " attestation(s) invalide(s)");
        }
        return i;
    }

    ClmDto.Integrite integrite(Trousse t) {
        List<EvenementTrousse> evts = evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId());
        ChaineScellee.Verification chaine = ChaineScellee.verifier(t.getId(), evts.stream().map(EvenementTrousse::maillon).toList());
        List<String> alteres = new ArrayList<>();
        for (DocumentTrousse d : documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())) {
            if (!ChaineScellee.sha256(d.getContenu()).equals(d.getEmpreinte())) {
                alteres.add(d.getNom() + " v" + d.getVersion());
            }
        }
        List<String> invalides = new ArrayList<>();
        Map<Long, Participant> parId = participantsParId(t);
        for (Invitation inv : invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            if (inv.getStatut() != Invitation.Statut.SIGNEE) {
                continue;
            }
            Map<String, Object> a = ctx.lireObjet(inv.getAttestationJson());
            Participant p = parId.get(inv.getParticipantId());
            FournisseurSignature.Attestation att = new FournisseurSignature.Attestation(str(a.get("fournisseur")), str(a.get("identifiant")),
                    Instant.parse(str(a.get("horodatage"))), str(a.get("jetonHorodatage")), str(a.get("algorithme")));
            boolean ok = p != null && fournisseur.verifier(new FournisseurSignature.Demande(str(a.get("empreinteDocuments")), inv.getNomSigne(),
                    p.getCourriel(), inv.getNiveau(), inv.getConsentementLe(), inv.getIp()), att);
            if (!ok) {
                invalides.add(inv.getNomSigne());
            }
        }
        boolean integre = chaine.integre() && alteres.isEmpty() && invalides.isEmpty();
        return new ClmDto.Integrite(integre, chaine.integre(), chaine.rupture().orElse(null), alteres, invalides, ctx.maintenant());
    }

    private List<ClmDto.SignatureVue> signatures(Trousse t) {
        Map<Long, Participant> parId = participantsParId(t);
        List<ClmDto.SignatureVue> res = new ArrayList<>();
        for (Invitation inv : invitations.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId())) {
            Participant p = parId.get(inv.getParticipantId());
            if (p == null || inv.getStatut() == Invitation.Statut.ANNULEE) {
                continue;
            }
            res.add(new ClmDto.SignatureVue(p.getNom(), p.getCourriel(), p.getRoleContrat(), inv.getNiveau().name(), inv.getVerifieeLe(),
                    inv.getConsentementLe(), inv.getSigneeLe(), inv.getIp(), inv.getStatut().name(), ctx.lireObjet(inv.getAttestationJson())));
        }
        return res;
    }

    /** Signature au nom d'une entreprise (addenda 4, Q6) : qualité, entreprise et attestation d'autorité, telles que signées. */
    static String representation(ClmDto.SignatureVue s) {
        Object r = s.attestation() == null ? null : s.attestation().get("representation");
        if (!(r instanceof Map<?, ?> m)) {
            return "";
        }
        String texte = " · pour " + m.get("entreprise") + " (" + m.get("qualite") + ")";
        if (Boolean.TRUE.equals(m.get("autoriteAttestee"))) {
            Object ref = m.get("referenceAutorite");
            texte += " · autorité attestée" + (ref == null || String.valueOf(ref).isBlank() ? "" : ", réf. " + ref);
        }
        return texte;
    }

    /** Archive portable : documents, certificat, journal, manifeste des empreintes et sceau du manifeste. */
    @Transactional
    public byte[] exporter(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        if (!Habilitations.peutModifier(t, acteur) && !acteur.estAuditeur()) {
            throw new org.springframework.security.access.AccessDeniedException("Export refusé par droits.");
        }
        journal.consigner(t, "PREUVE_EXPORTEE", acteur.id(), null);
        Map<String, byte[]> fichiers = new LinkedHashMap<>();
        for (DocumentTrousse d : documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())) {
            if (d.getStatut() != DocumentTrousse.Statut.RETIRE) {
                String chemin = "documents/" + d.getCleDocument() + "/v" + d.getVersion() + "-" + d.getStatut().name().toLowerCase()
                        + "-" + d.getNom();
                fichiers.put(chemin, d.getContenu());
                if (d.getStatut() == DocumentTrousse.Statut.SIGNE) {
                    // Signature CMS détachée (CAdES) du document final : vérifiable sans la plateforme.
                    fichiers.put(chemin + ".p7s", cachet.signerDetache(d.getContenu()));
                }
            }
        }
        ClmDto.PreuveVue dossier = dossier(trousseId, acteur);
        fichiers.put("certificat.pdf", certificatScelle(t, dossier));
        fichiers.put("certificat.json", json(Map.of("trousse", dossier.trousse(), "documents", dossier.documentsFinaux(),
                "signatures", dossier.signatures(), "integrite", dossier.integrite(), "genereLe", ctx.maintenant().toString(),
                "cachet", Map.of("titulaire", cachet.titulaire(), "normes", "CAdES (RFC 5652) · " + cachet.niveauPades() + " · PDF/A-2b · RFC 3161 si configuré",
                        "demonstration", cachet.demonstration()))));
        fichiers.put("journal.json", json(dossier.evenements()));
        fichiers.put("LISEZMOI.txt", lisezMoi(t).getBytes(StandardCharsets.UTF_8));
        Map<String, String> manifeste = new LinkedHashMap<>();
        fichiers.forEach((nom, contenu) -> manifeste.put(nom, ChaineScellee.sha256(contenu)));
        byte[] manifesteOctets = json(manifeste);
        fichiers.put("manifeste.json", manifesteOctets);
        fichiers.put("sceau.json", json(Map.of("algorithme", "HMAC-SHA256", "portee", "manifeste.json",
                "sceau", scelleur.sceller(manifesteOctets), "scelleLe", ctx.maintenant().toString())));
        ByteArrayOutputStream archive = new ByteArrayOutputStream();
        archiver(fichiers, archive);
        return archive.toByteArray();
    }

    /** Certificat lisible et durable (PDF) : documents finaux, signataires, identités, consentements, intégrité. */
    @Transactional
    public byte[] certificat(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        byte[] pdf = certificatScelle(t, dossier(trousseId, acteur));
        journal.consigner(t, "CERTIFICAT_TELECHARGE", acteur.id(), null);
        return pdf;
    }

    /** Certificat PDF signé en PAdES par le cachet de l'organisation : toute altération invalide la signature. */
    private byte[] certificatScelle(Trousse t, ClmDto.PreuveVue dossier) {
        String texte = texteCertificat(t, dossier) + "\nCachet électronique : " + cachet.titulaire()
                + (cachet.demonstration() ? " (certificat de démonstration)" : "") + " — signature PAdES de ce document.\n";
        return cachet.scellerPdf(rendu.genererPdf("Certificat de signature — " + t.getReference(), texte), "Certificat de signature " + t.getReference());
    }

    static String texteCertificat(Trousse t, ClmDto.PreuveVue d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trousse : ").append(t.getReference()).append(" — ").append(t.getTitre()).append('\n')
                .append("État : ").append(t.getStatut()).append(" · Source : ").append(t.getSource())
                .append(t.getSfDossierId() == null ? "" : " " + t.getSfDossierId()).append("\n\nDocuments\n");
        d.documentsFinaux().forEach(doc -> sb.append("- ").append(doc.nom()).append(" v").append(doc.version()).append(" (")
                .append(doc.statut()).append(") SHA-256 ").append(doc.empreinte()).append('\n'));
        sb.append("\nSignataires\n");
        for (ClmDto.SignatureVue s : d.signatures()) {
            sb.append("- ").append(s.participant()).append(" <").append(s.courriel()).append("> · ").append(s.roleContrat())
                    .append(" · identité ").append(s.niveau()).append(" vérifiée ").append(s.verifieeLe())
                    .append(" · consentement ").append(s.consentementLe()).append(" · signé ").append(s.signeeLe())
                    .append(" · ").append(s.statut());
            Object attestation = s.attestation() == null ? null : s.attestation().get("identifiant");
            sb.append(attestation == null ? "" : " · attestation " + attestation).append(representation(s)).append('\n');
        }
        ClmDto.Integrite i = d.integrite();
        sb.append("\nIntégrité : ").append(i.integre() ? "confirmée" : "ALTÉRATION DÉTECTÉE").append(" (journal de ")
                .append(d.evenements().size()).append(" événements, vérifié le ").append(i.verifieeLe()).append(")\n");
        return sb.toString();
    }

    private static String lisezMoi(Trousse t) {
        return "Dossier de preuve de la trousse " + t.getReference() + "\n\n"
                + "1. manifeste.json liste l'empreinte SHA-256 de chaque fichier de l'archive.\n"
                + "2. sceau.json scelle manifeste.json (HMAC-SHA256, clé détenue par l'organisme) : la vérification se fait\n"
                + "   via l'API /api/clm/trousses/{id}/preuve/integrite ou par l'organisme.\n"
                + "3. journal.json est une chaîne d'empreintes : chaque événement contient l'empreinte du précédent.\n"
                + "4. certificat.json décrit signataires, niveaux d'identité, consentements et horodatages ;\n"
                + "   certificat.pdf en est la version lisible et durable, signée en PAdES (vérifiable dans tout lecteur PDF).\n"
                + "5. Chaque document signé est accompagné de sa signature CMS détachée (.p7s, CAdES) :\n"
                + "   openssl cms -verify -inform DER -binary -in <document>.p7s -content <document> -noverify\n"
                + "   (-noverify omet la chaîne de confiance ; avec le certificat de l'autorité, utiliser -CAfile).\n";
    }

    private Map<Long, Participant> participantsParId(Trousse t) {
        Map<Long, Participant> m = new LinkedHashMap<>();
        participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(p -> m.put(p.getId(), p));
        return m;
    }

    private byte[] json(Object o) {
        return ctx.ecrireLisible(o);
    }

    /** Écrit l'archive dans {@code sortie} ; une écriture interrompue ne livre jamais une archive partielle. */
    static void archiver(Map<String, byte[]> fichiers, OutputStream sortie) {
        try (ZipOutputStream zip = new ZipOutputStream(sortie)) {
            for (Map.Entry<String, byte[]> f : fichiers.entrySet()) {
                zip.putNextEntry(new ZipEntry(f.getKey()));
                zip.write(f.getValue());
                zip.closeEntry();
            }
            zip.finish();
        } catch (IOException e) {
            throw new IllegalStateException("Archive de preuve impossible", e);
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
