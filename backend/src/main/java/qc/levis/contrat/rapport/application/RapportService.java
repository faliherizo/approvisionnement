package qc.levis.contrat.rapport.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rapport.application.repository.CritereRechercheSauveRepository;
import qc.levis.contrat.rapport.domain.CritereRechercheSauve;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;

/**
 * Rapports : critères de recherche sauvegardés, résumé (tableau de bord) et export Excel des fiches.
 * L'export aplatit les réponses JSON en colonnes dynamiques (aucune colonne figée).
 */
@Service("rapportFormulaireService")
public class RapportService {

    private final CritereRechercheSauveRepository criteres;
    private final SaisieRepository saisies;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public RapportService(CritereRechercheSauveRepository criteres, SaisieRepository saisies) {
        this.criteres = criteres;
        this.saisies = saisies;
    }

    public List<CritereRechercheSauve> mesCriteres(String utilisateur) {
        return criteres.findByTenantIdAndUtilisateur(TenantContext.get(), utilisateur);
    }

    @Transactional
    public CritereRechercheSauve sauvegarder(String utilisateur, Long formulaireId, String nom, String criteresJson) {
        return criteres.save(new CritereRechercheSauve(TenantContext.get(), utilisateur, formulaireId, nom, criteresJson));
    }

    @Transactional
    public void supprimerCritere(Long id) { criteres.deleteById(id); }

    /** Résumé pour tableau de bord : nombre de fiches par statut. */
    public Map<String, Long> resume(Long formulaireId) {
        return saisies.findByTenantIdAndFormulaireIdOrderByMajLeDesc(TenantContext.get(), formulaireId).stream()
                .collect(Collectors.groupingBy(s -> s.getStatut().name(), Collectors.counting()));
    }

    /** Export Excel (XLSX) des fiches d'un formulaire : colonnes = méta + clés de réponses rencontrées. */
    public byte[] exporterExcel(Long formulaireId) {
        List<Saisie> fiches = saisies.findByTenantIdAndFormulaireIdOrderByMajLeDesc(TenantContext.get(), formulaireId);
        LinkedHashSet<String> colonnes = new LinkedHashSet<>(List.of("id", "statut", "soumisLe"));
        List<Map<String, Object>> lignes = new ArrayList<>();
        for (Saisie s : fiches) {
            Map<String, Object> donnees = parse(s.getDonneesJson());
            colonnes.addAll(donnees.keySet());
            Map<String, Object> ligne = new HashMap<>(donnees);
            ligne.put("id", s.getId());
            ligne.put("statut", s.getStatut().name());
            ligne.put("soumisLe", s.getSoumisLe() != null ? s.getSoumisLe().toString() : "");
            lignes.add(ligne);
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Fiches");
            Row entete = sheet.createRow(0);
            List<String> cols = new ArrayList<>(colonnes);
            for (int c = 0; c < cols.size(); c++) entete.createCell(c).setCellValue(cols.get(c));
            for (int r = 0; r < lignes.size(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < cols.size(); c++) {
                    Object v = lignes.get(r).get(cols.get(c));
                    row.createCell(c).setCellValue(v == null ? "" : String.valueOf(v));
                }
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Échec de l'export Excel", e);
        }
    }

    private Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try { return mapper.readValue(json, new TypeReference<Map<String, Object>>() {}); }
        catch (Exception e) { return Map.of(); }
    }
}
