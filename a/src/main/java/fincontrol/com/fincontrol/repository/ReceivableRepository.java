package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReceivableRepository extends JpaRepository<Receivable, UUID> {
}
