package qc.levis.contrat.addenda.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import qc.levis.contrat.addenda.domain.Addenda;

public interface AddendaRepository extends JpaRepository<Addenda, Long> {
    List<Addenda> findByDocumentParentIdOrderByNumeroAsc(Long documentParentId);
}
