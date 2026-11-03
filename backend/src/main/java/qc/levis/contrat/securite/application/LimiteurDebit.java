package qc.levis.contrat.securite.application;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteur de débit à fenêtre glissante (anti-abus / anti-force brute), déterministe vis-à-vis du temps
 * fourni → testable. {@code autoriser} renvoie faux lorsque le nombre d'appels pour une clé dépasse le
 * maximum sur la fenêtre.
 */
public class LimiteurDebit {

    private final Map<String, Deque<Long>> historique = new ConcurrentHashMap<>();

    public boolean autoriser(String cle, int maxParFenetre, long fenetreMs, long maintenant) {
        Deque<Long> dq = historique.computeIfAbsent(cle, k -> new ArrayDeque<>());
        synchronized (dq) {
            long limite = maintenant - fenetreMs;
            while (!dq.isEmpty() && dq.peekFirst() < limite) dq.pollFirst();
            if (dq.size() >= maxParFenetre) return false;
            dq.addLast(maintenant);
            return true;
        }
    }
}
