package qc.levis.contrat.clm.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Exportation vers un référentiel désigné par IQ (addenda 2, Q13) : les documents signés, preuves et pistes d'audit
 * restent dans la solution, mais un lot de trousses closes peut en sortir à tout moment. Chaque trousse y figure sous
 * la forme de son dossier de preuve autoportant ; un index (JSON et CSV) en décrit les métadonnées pour le chargement
 * dans le référentiel cible (SharePoint, GED, archives), sans dépendre de la plateforme.
 */
@Service
public class ExportationReferentielService {

    /** Un lot reste téléchargeable en une fois ; au-delà, l'administrateur découpe la période. */
    public static final int LOT_MAX = 200;
    private static final EnumSet<StatutTrousse> CLOSES = EnumSet.of(StatutTrousse.TERMINEE, StatutTrousse.ANNULEE);

    private final TrousseRepository trousses;
    private final PreuveService preuves;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ExportationReferentielService(TrousseRepository trousses, PreuveService preuves, JournalClm journal, ContexteClm ctx) {
        this.trousses = trousses;
        this.preuves = preuves;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Trousses closes entre {@code du} et {@code au} inclusivement (date de clôture), non détruites. */
    @Transactional
    public byte[] exporter(Habilitations.Acteur acteur, LocalDate du, LocalDate au) {
        if (!acteur.estAdmin()) {
            throw new AccessDeniedException("Exportation réservée à l'administration.");
        }
        if (du == null || au == null || au.isBefore(du)) {
            throw new IllegalArgumentException("Période d'exportation invalide : « du » et « au » requis, « au » postérieur à « du ».");
        }
        List<Trousse> lot = trousses.findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(ctx.tenant(), CLOSES).stream()
                .filter(t -> dansPeriode(t, du, au)).toList();
        if (lot.size() > LOT_MAX) {
            throw new IllegalArgumentException("Lot de " + lot.size() + " trousses : " + LOT_MAX + " au plus, réduire la période.");
        }
        Map<String, byte[]> fichiers = new LinkedHashMap<>();
        List<Map<String, Object>> index = new ArrayList<>();
        for (Trousse t : lot) {
            byte[] preuve = preuves.exporter(t.getId(), acteur);
            String chemin = "trousses/" + nomFichier(t.getReference()) + ".zip";
            fichiers.put(chemin, preuve);
            index.add(entree(t, chemin, preuve));
        }
        fichiers.put("index.json", ctx.ecrire(Map.of("organisation", ctx.tenant(), "du", du.toString(), "au", au.toString(),
                "genereLe", ctx.maintenant().toString(), "trousses", index)).getBytes(StandardCharsets.UTF_8));
        fichiers.put("index.csv", csv(index).getBytes(StandardCharsets.UTF_8));
        journal.configuration(acteur.id(), "EXPORTATION_REFERENTIEL", du + " → " + au + " · " + lot.size() + " trousse(s)");
        return zip(fichiers);
    }

    private boolean dansPeriode(Trousse t, LocalDate du, LocalDate au) {
        if (t.getClotureLe() == null) {
            return false;
        }
        LocalDate cloture = t.getClotureLe().atZone(ctx.fuseau()).toLocalDate();
        return !cloture.isBefore(du) && !cloture.isAfter(au);
    }

    private static Map<String, Object> entree(Trousse t, String chemin, byte[] preuve) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("reference", t.getReference());
        e.put("titre", t.getTitre());
        e.put("typeContrat", t.getTypeContrat());
        e.put("statut", t.getStatut().name());
        e.put("clotureLe", t.getClotureLe().toString());
        e.put("sfDossierId", t.getSfDossierId());
        e.put("fichier", chemin);
        e.put("sha256", ChaineScellee.sha256(preuve));
        return e;
    }

    /** Les cellules commençant par un caractère de formule sont neutralisées, comme dans l'export du suivi. */
    static String csv(List<Map<String, Object>> index) {
        StringBuilder sb = new StringBuilder("reference;titre;typeContrat;statut;clotureLe;sfDossierId;fichier;sha256\n");
        for (Map<String, Object> e : index) {
            sb.append(String.join(";", e.values().stream().map(ExportationReferentielService::cellule).toList())).append('\n');
        }
        return sb.toString();
    }

    private static String cellule(Object v) {
        String s = v == null ? "" : String.valueOf(v);
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return s.contains(";") || s.contains("\"") || s.contains("\n") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    static String nomFichier(String reference) {
        return reference.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static byte[] zip(Map<String, byte[]> fichiers) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(sortie)) {
            for (Map.Entry<String, byte[]> f : fichiers.entrySet()) {
                z.putNextEntry(new ZipEntry(f.getKey()));
                z.write(f.getValue());
                z.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sortie.toByteArray();
    }
}
