package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Payable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayableRepository extends JpaRepository<Payable, UUID> {
}
