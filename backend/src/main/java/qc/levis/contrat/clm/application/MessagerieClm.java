package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.application.repository.ModeleMessageRepository;
import qc.levis.contrat.clm.domain.ModeleMessage;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;

/**
 * Notifications CLM personnalisables (ST-11, UI-17). Sans message configuré pour un événement, un texte par
 * défaut est utilisé : aucune notification réglementaire ne peut être perdue faute de configuration.
 */
@Service
public class MessagerieClm {

    public static final List<String> CANAUX = List.of("COURRIEL", "SMS", "INTERNE");
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([\\w.-]+)\\s*}}");
    private static final Map<Evenement, String[]> DEFAUTS = new EnumMap<>(Evenement.class);

    static {
        DEFAUTS.put(Evenement.INVITATION, new String[]{"Document à signer : {{titre}}",
                "Bonjour {{destinataire}},\nVous êtes invité à signer « {{titre}} » ({{reference}}).\nLien sécurisé : {{lien}}\nCe lien expire le {{echeance}}."});
        DEFAUTS.put(Evenement.TACHE, new String[]{"Action requise : {{etape}} — {{reference}}",
                "Une action vous est assignée sur la trousse « {{titre}} » ({{reference}}) : {{etape}}. Échéance : {{echeance}}."});
        DEFAUTS.put(Evenement.RAPPEL, new String[]{"Rappel : {{etape}} — {{reference}}",
                "Rappel : l'étape « {{etape}} » de la trousse {{reference}} attend toujours votre réponse. Échéance : {{echeance}}."});
        DEFAUTS.put(Evenement.ECHEANCE, new String[]{"Échéance proche : {{etape}} — {{reference}}",
                "L'étape « {{etape}} » de la trousse {{reference}} arrive à échéance le {{echeance}}. Merci d'y donner suite avant cette date."});
        DEFAUTS.put(Evenement.RETARD, new String[]{"Échéance dépassée — {{reference}}",
                "L'étape « {{etape}} » de la trousse {{reference}} a dépassé son échéance ({{echeance}})."});
        DEFAUTS.put(Evenement.REJET, new String[]{"Trousse rejetée — {{reference}}",
                "La trousse « {{titre}} » ({{reference}}) a été rejetée ou retournée : {{motif}}"});
        DEFAUTS.put(Evenement.TERMINEE, new String[]{"Signature terminée — {{reference}}",
                "Toutes les signatures de « {{titre}} » ({{reference}}) sont recueillies. Le dossier de preuve est disponible."});
        DEFAUTS.put(Evenement.CODE, new String[]{"Code de vérification",
                "Votre code de vérification pour {{reference}} : {{code}}. Il expire dans 10 minutes."});
    }

    private final ModeleMessageRepository modeles;
    private final NotificateurClm notificateur;
    private final ContexteClm ctx;

    public MessagerieClm(ModeleMessageRepository modeles, NotificateurClm notificateur, ContexteClm ctx) {
        this.modeles = modeles;
        this.notificateur = notificateur;
        this.ctx = ctx;
    }

    /** Envoie l'événement sur le canal demandé, puis à chaque adresse en copie. Retourne le statut du premier envoi. */
    public String envoyer(Long tenantId, Evenement evenement, String canal, String destinataire, Map<String, Object> variables,
                          List<String> copies) {
        String[] texte = gabarit(tenantId, evenement, canal);
        if (texte == null) {
            return "DESACTIVE";
        }
        String sujet = fusionner(texte[0], variables);
        String corps = fusionner(texte[1], variables);
        String statut = notificateur.envoyer(canal, destinataire, sujet, corps);
        for (String copie : copies == null ? List.<String>of() : copies) {
            notificateur.envoyer("COURRIEL", copie, "[Copie] " + sujet, corps);
        }
        return statut;
    }

    private String[] gabarit(Long tenantId, Evenement evenement, String canal) {
        for (ModeleMessage m : modeles.findByTenantIdAndEvenement(tenantId, evenement)) {
            if (m.getCanal().equals(canal)) {
                return m.isActif() ? new String[]{m.getSujet(), m.getCorps()} : null;
            }
        }
        return DEFAUTS.get(evenement);
    }

    // --- Administration (UI-17) ---
    public List<ClmDto.MessageVue> lister() {
        Long tenant = ctx.tenant();
        List<ClmDto.MessageVue> res = new ArrayList<>();
        for (Evenement e : Evenement.values()) {
            for (String canal : CANAUX) {
                ModeleMessage m = modeles.findByTenantIdAndEvenement(tenant, e).stream()
                        .filter(x -> x.getCanal().equals(canal)).findFirst().orElse(null);
                res.add(m == null
                        ? new ClmDto.MessageVue(null, e.name(), canal, DEFAUTS.get(e)[0], DEFAUTS.get(e)[1], true)
                        : new ClmDto.MessageVue(m.getId(), e.name(), canal, m.getSujet(), m.getCorps(), m.isActif()));
            }
        }
        return res;
    }

    @Transactional
    public ClmDto.MessageVue enregistrer(ClmDto.MessageRequest req) {
        Long tenant = ctx.tenant();
        Evenement e = Evenement.valueOf(exiger(req.evenement(), "Événement requis."));
        String canal = exiger(req.canal(), "Canal requis.");
        if (!CANAUX.contains(canal)) {
            throw new IllegalArgumentException("Canal non disponible : " + canal);
        }
        String sujet = exiger(req.sujet(), "Sujet requis.");
        String corps = exiger(req.corps(), "Corps requis.");
        List<String> inconnues = variablesInconnues(sujet + " " + corps);
        if (!inconnues.isEmpty()) {
            throw new IllegalArgumentException("Variable non résolue : " + String.join(", ", inconnues));
        }
        ModeleMessage m = modeles.findByTenantIdAndEvenement(tenant, e).stream()
                .filter(x -> x.getCanal().equals(canal)).findFirst()
                .orElseGet(() -> new ModeleMessage(tenant, e, canal, sujet, corps));
        m.setSujet(sujet);
        m.setCorps(corps);
        m.setActif(req.actif() == null || req.actif());
        m = modeles.save(m);
        return new ClmDto.MessageVue(m.getId(), e.name(), canal, m.getSujet(), m.getCorps(), m.isActif());
    }

    public String tester(String evenement, ClmDto.TestMessageRequest req) {
        String canal = exiger(req.canal(), "Canal requis.");
        String destinataire = exiger(req.destinataire(), "Adresse invalide.");
        if ("COURRIEL".equals(canal) && !destinataire.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Adresse invalide.");
        }
        Map<String, Object> exemple = Map.of("reference", "TR-EXEMPLE", "titre", "Contrat d'exemple", "etape", "Approbation",
                "destinataire", destinataire, "lien", "https://exemple/portail", "echeance", "2026-12-31", "code", "123456",
                "motif", "exemple");
        return envoyer(ctx.tenant(), Evenement.valueOf(evenement), canal, destinataire, exemple, List.of());
    }

    static List<String> variablesInconnues(String texte) {
        List<String> res = new ArrayList<>();
        Matcher m = VARIABLE.matcher(texte);
        while (m.find()) {
            if (!ModeleMessage.VARIABLES.contains(m.group(1))) {
                res.add(m.group(1));
            }
        }
        return res;
    }

    static String fusionner(String gabarit, Map<String, Object> valeurs) {
        Matcher m = VARIABLE.matcher(gabarit);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object v = valeurs.get(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(v == null ? "" : String.valueOf(v)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String exiger(String v, String message) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return v.trim();
    }
}
