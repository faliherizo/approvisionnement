package qc.levis.contrat.securite.application;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.securite.application.repository.*;
import qc.levis.contrat.securite.domain.*;

/**
 * Sécurité fine : permissions granulaires et groupes. L'évaluation d'accès est déléguée au moteur pur
 * {@link MoteurAcces} (refus par défaut). Tenant-scopé.
 */
@Service
public class SecuriteService {

    private final PermissionAccesRepository permissions;
    private final GroupeRepository groupes;
    private final GroupeMembreRepository membres;

    public SecuriteService(PermissionAccesRepository permissions, GroupeRepository groupes, GroupeMembreRepository membres) {
        this.permissions = permissions;
        this.groupes = groupes;
        this.membres = membres;
    }

    public List<RegleAcces> regles(Long tenantId) {
        return permissions.findByTenantId(tenantId).stream().map(PermissionAcces::versRegle).toList();
    }

    public Set<String> groupesDe(Long tenantId, String utilisateur) {
        return membres.findByTenantIdAndUtilisateur(tenantId, utilisateur).stream()
                .map(GroupeMembre::getGroupeCode).collect(Collectors.toSet());
    }

    /** Décision d'accès : refus par défaut, déléguée au moteur pur. */
    public boolean autorise(Long tenantId, Set<String> roles, String utilisateur,
                            String portee, String cible, String action) {
        SujetsUtilisateur u = new SujetsUtilisateur(roles, groupesDe(tenantId, utilisateur), utilisateur);
        return MoteurAcces.autorise(regles(tenantId), u, portee, cible, action);
    }

    // --- CRUD ---
    public List<PermissionAcces> listerPermissions(Long tenantId) { return permissions.findByTenantId(tenantId); }
    @Transactional public PermissionAcces ajouterPermission(PermissionAcces p) { return permissions.save(p); }
    @Transactional public void supprimerPermission(Long id) { permissions.deleteById(id); }
    public List<Groupe> listerGroupes(Long tenantId) { return groupes.findByTenantId(tenantId); }
    @Transactional public Groupe ajouterGroupe(Groupe g) { return groupes.save(g); }
    @Transactional public GroupeMembre ajouterMembre(GroupeMembre m) { return membres.save(m); }
}
