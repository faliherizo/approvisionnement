package qc.levis.contrat.soutien.infrastructure.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.soutien.application.BanqueHeuresService;
import qc.levis.contrat.soutien.application.dto.BanqueVue;
import qc.levis.contrat.soutien.domain.Mouvement;

/** Banque d'heures vue par l'organisation (CHU 9.05) : solde, répartition par catégorie, relevé des mouvements. */
@RestController
@RequestMapping("/api/soutien/banque-heures")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTION')")
public class BanqueHeuresController {

    private final BanqueHeuresService service;

    public BanqueHeuresController(BanqueHeuresService service) {
        this.service = service;
    }

    @GetMapping
    public List<BanqueVue> lister() {
        return service.lister(TenantContext.get());
    }

    @GetMapping("/{id}")
    public BanqueVue consulter(@PathVariable Long id) {
        return service.consulter(TenantContext.get(), id);
    }

    @GetMapping("/{id}/releve.csv")
    public ResponseEntity<byte[]> releve(@PathVariable Long id) {
        BanqueVue b = service.consulter(TenantContext.get(), id);
        String lignes = b.mouvements().stream().map(BanqueHeuresController::ligne).collect(Collectors.joining("\r\n"));
        byte[] corps = ("﻿date;type;categorie;heures;description;intervenant\r\n" + lignes + "\r\n").getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"releve-banque-" + id + ".csv\"").body(corps);
    }

    private static String ligne(Mouvement m) {
        return String.join(";", cellule(m.getDate()), cellule(m.getType()), cellule(m.getCategorie()), cellule(m.getHeures()),
                cellule(m.getDescription()), cellule(m.getIntervenant()));
    }

    /** Guillemets doublés et formules neutralisées (=, +, -, @) : un tableur n'exécute rien à l'ouverture. */
    static String cellule(Object valeur) {
        if (valeur == null) return "";
        String v = String.valueOf(valeur);
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
