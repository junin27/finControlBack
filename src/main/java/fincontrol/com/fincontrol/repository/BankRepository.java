package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {
}
