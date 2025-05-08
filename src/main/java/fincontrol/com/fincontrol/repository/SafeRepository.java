package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Safe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SafeRepository extends JpaRepository<Safe, UUID> {
}
