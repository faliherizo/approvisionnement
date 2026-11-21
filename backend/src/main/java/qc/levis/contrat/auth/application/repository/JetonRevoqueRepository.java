package qc.levis.contrat.auth.application.repository;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qc.levis.contrat.auth.domain.JetonRevoque;

public interface JetonRevoqueRepository extends JpaRepository<JetonRevoque, String> {
    @Modifying
    @Query("delete from JetonRevoque j where j.expireLe < :maintenant")
    int purger(@Param("maintenant") Instant maintenant);
}
