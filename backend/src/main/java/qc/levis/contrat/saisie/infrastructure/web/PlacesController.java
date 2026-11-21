package qc.levis.contrat.saisie.infrastructure.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.saisie.application.PlacesService;
import qc.levis.contrat.saisie.application.dto.InventaireVue;
import qc.levis.contrat.saisie.application.dto.PlaceVue;
import qc.levis.contrat.saisie.domain.InventaireChoix;
import qc.levis.contrat.saisie.domain.PlaceChoix;

/**
 * Places des choix (CHU 3.06) et inventaire des inscriptions (CHU 4.08). Les places, sans renseignement personnel, sont
 * lisibles par qui remplit le formulaire ; l'inventaire, qui nomme les demandeurs, est réservé au personnel qui traite.
 */
@RestController
@RequestMapping("/api/formulaires/{fid}")
@PreAuthorize("@modules.actif('saisie')")
public class PlacesController {

    private final PlacesService service;

    public PlacesController(PlacesService service) {
        this.service = service;
    }

    @GetMapping("/places")
    public List<PlaceVue> places(@PathVariable Long fid) {
        return service.plan(fid).places().stream().map(PlaceVue::de).toList();
    }

    @GetMapping("/inventaire")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
    public List<InventaireVue> inventaire(@PathVariable Long fid) {
        return service.inventaire(fid).stream().map(InventaireVue::de).toList();
    }

    @GetMapping(value = "/inventaire.csv")
    @PreAuthorize("@modules.actif('saisie') and hasAnyRole('ADMIN','APPRO','DIRECTION','REDACTEUR')")
    public ResponseEntity<byte[]> inventaireCsv(@PathVariable Long fid) {
        String entete = "champ;choix;capacite;inscrits;restantes;fiche;statut;soumis_le;demandeur";
        String lignes = service.inventaire(fid).stream().flatMap(PlacesController::lignes).collect(Collectors.joining("\r\n"));
        byte[] corps = ("﻿" + entete + "\r\n" + lignes + "\r\n").getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventaire-" + fid + ".csv\"")
                .body(corps);
    }

    private static Stream<String> lignes(InventaireChoix i) {
        PlaceChoix p = i.place();
        String debut = String.join(";", cellule(p.libelleChamp()), cellule(p.choix()), cellule(p.capacite()),
                cellule(p.inscrits()), cellule(p.restantes()));
        if (i.inscriptions().isEmpty()) return Stream.of(debut + ";;;;");
        return i.inscriptions().stream().map(f -> debut + ";" + String.join(";", cellule(f.numero()), cellule(f.statut()),
                cellule(f.soumisLe()), cellule(f.demandeur())));
    }

    /**
     * Cellule CSV : guillemets doublés, et neutralisation des formules (=, +, -, @) qu'un tableur exécuterait à
     * l'ouverture (injection CSV, OWASP).
     */
    static String cellule(Object valeur) {
        if (valeur == null) return "";
        String v = String.valueOf(valeur);
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
