package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.PayableInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayableInstallmentRepository extends JpaRepository<PayableInstallment, UUID> {
}
