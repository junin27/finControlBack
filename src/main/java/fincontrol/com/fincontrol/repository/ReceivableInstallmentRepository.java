package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.ReceivableInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReceivableInstallmentRepository extends JpaRepository<ReceivableInstallment, UUID> {
}
