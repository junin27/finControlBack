package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // Importar List
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {
    // Novo método para buscar bancos por ID do usuário
    List<Bank> findAllByUserId(UUID userId);
}