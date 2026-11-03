package qc.levis.contrat.clm.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public PreuveService(DocumentTrousseRepository documents, InvitationRepository invitations, ParticipantRepository participants,
                         EvenementTrousseRepository evenements, FournisseurSignature fournisseur, Scelleur scelleur,
                         RenduDocuments rendu, LectureTrousse lecture, JournalClm journal, ContexteClm ctx) {
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
    }

    public ClmDto.PreuveVue dossier(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        Instant maintenant = ctx.maintenant();
        List<ClmDto.DocumentVue> finaux = documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())
                .stream().filter(d -> d.getStatut() == DocumentTrousse.Statut.SIGNE || d.getStatut() == DocumentTrousse.Statut.ENVOYE)
                .map(LectureTrousse::document).toList();
        return new ClmDto.PreuveVue(lecture.resume(t, List.of(), maintenant), finaux, signatures(t),
                evenements.findByTenantIdAndTrousseIdOrderBySequenceAsc(t.getTenantId(), t.getId()).stream()
                        .map(LectureTrousse::evenement).toList(), integrite(t));
    }

    @Transactional
    public ClmDto.Integrite verifier(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        ClmDto.Integrite i = integrite(t);
        journal.consigner(t, "INTEGRITE_VERIFIEE", acteur.id(), i.integre() ? "intègre" : "ALTÉRATION DÉTECTÉE");
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

    /** Archive portable : documents, certificat, journal, manifeste des empreintes et sceau du manifeste. */
    @Transactional
    public byte[] exporter(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        if (!Habilitations.peutModifier(t, acteur) && !acteur.roles().contains("AUDITEUR")) {
            throw new org.springframework.security.access.AccessDeniedException("Export refusé par droits.");
        }
        journal.consigner(t, "PREUVE_EXPORTEE", acteur.id(), null);
        Map<String, byte[]> fichiers = new LinkedHashMap<>();
        for (DocumentTrousse d : documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId())) {
            if (d.getStatut() != DocumentTrousse.Statut.RETIRE) {
                fichiers.put("documents/" + d.getCleDocument() + "/v" + d.getVersion() + "-" + d.getStatut().name().toLowerCase()
                        + "-" + d.getNom(), d.getContenu());
            }
        }
        ClmDto.PreuveVue dossier = dossier(trousseId, acteur);
        fichiers.put("certificat.pdf", rendu.genererPdf("Certificat de signature — " + t.getReference(), texteCertificat(t, dossier)));
        fichiers.put("certificat.json", json(Map.of("trousse", dossier.trousse(), "documents", dossier.documentsFinaux(),
                "signatures", dossier.signatures(), "integrite", dossier.integrite(), "genereLe", ctx.maintenant().toString())));
        fichiers.put("journal.json", json(dossier.evenements()));
        fichiers.put("LISEZMOI.txt", lisezMoi(t).getBytes(StandardCharsets.UTF_8));
        Map<String, String> manifeste = new LinkedHashMap<>();
        fichiers.forEach((nom, contenu) -> manifeste.put(nom, ChaineScellee.sha256(contenu)));
        byte[] manifesteOctets = json(manifeste);
        fichiers.put("manifeste.json", manifesteOctets);
        fichiers.put("sceau.json", json(Map.of("algorithme", "HMAC-SHA256", "portee", "manifeste.json",
                "sceau", scelleur.sceller(manifesteOctets), "scelleLe", ctx.maintenant().toString())));
        return zip(fichiers);
    }

    /** Certificat lisible et durable (PDF) : documents finaux, signataires, identités, consentements, intégrité. */
    @Transactional
    public byte[] certificat(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        byte[] pdf = rendu.genererPdf("Certificat de signature — " + t.getReference(), texteCertificat(t, dossier(trousseId, acteur)));
        journal.consigner(t, "CERTIFICAT_TELECHARGE", acteur.id(), null);
        return pdf;
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
            sb.append(attestation == null ? "" : " · attestation " + attestation).append('\n');
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
                + "   certificat.pdf en est la version lisible et durable.\n";
    }

    private Map<Long, Participant> participantsParId(Trousse t) {
        Map<Long, Participant> m = new LinkedHashMap<>();
        participants.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).forEach(p -> m.put(p.getId(), p));
        return m;
    }

    private byte[] json(Object o) {
        try {
            return ctx.json().writerWithDefaultPrettyPrinter().writeValueAsBytes(o);
        } catch (IOException e) {
            throw new IllegalStateException("Sérialisation de la preuve impossible", e);
        }
    }

    private static byte[] zip(Map<String, byte[]> fichiers) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> f : fichiers.entrySet()) {
                zip.putNextEntry(new ZipEntry(f.getKey()));
                zip.write(f.getValue());
                zip.closeEntry();
            }
            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Archive de preuve impossible", e);
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
