package qc.levis.contrat.clm.infrastructure;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.CoffreDocuments;
import qc.levis.contrat.clm.domain.DocumentTrousse;

/**
 * Chiffrement au repos transparent du contenu des documents (Annexe 10, SEC-28 ; ADR-037). À l'insertion, la forme stockée
 * devient l'enveloppe chiffrée par la clé de l'organisation ; au chargement, le clair est rétabli en mémoire, sans
 * modifier l'entité suivie (aucune écriture parasite). Les cas d'utilisation lisent et produisent du clair, comme avant ;
 * seule la base ne voit que des enveloppes. Les écouteurs se branchent sur Hibernate pour que le domaine reste sans
 * dépendance vers l'infrastructure.
 */
@Component
public class ChiffrementAuRepos implements PreInsertEventListener, PostLoadEventListener {

    static final String CONTENU = "contenu";
    static final String VERSION = "cleVersion";

    private final EntityManagerFactory emf;
    private final CoffreDocuments coffre;

    public ChiffrementAuRepos(EntityManagerFactory emf, CoffreDocuments coffre) {
        this.emf = emf;
        this.coffre = coffre;
    }

    @PostConstruct
    void brancher() {
        EventListenerRegistry registre = emf.unwrap(SessionFactoryImplementor.class).getServiceRegistry()
                .requireService(EventListenerRegistry.class);
        registre.appendListeners(EventType.PRE_INSERT, this);
        registre.appendListeners(EventType.POST_LOAD, this);
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        if (event.getEntity() instanceof DocumentTrousse d && d.getCleVersion() == null) {
            CoffreDocuments.Chiffre c = coffre.chiffrer(d.getTenantId(), d.getStockage());
            d.stockerChiffre(c.octets(), c.version());
            String[] proprietes = event.getPersister().getPropertyNames();
            event.getState()[indice(proprietes, CONTENU)] = c.octets();
            event.getState()[indice(proprietes, VERSION)] = c.version();
        }
        return false;
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        if (event.getEntity() instanceof DocumentTrousse d) {
            d.dechiffre(coffre.dechiffrer(d.getTenantId(), d.getStockage()));
        }
    }

    static int indice(String[] proprietes, String nom) {
        int i = Arrays.asList(proprietes).indexOf(nom);
        if (i < 0) {
            throw new IllegalStateException("Propriété absente du document : " + nom);
        }
        return i;
    }
}
