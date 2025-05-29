package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // Importar Optional
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {
    // Método para buscar todos os bancos por ID do usuário
    List<Bank> findAllByUserId(UUID userId);

    // NOVO MÉTODO: para buscar um banco específico pelo seu ID e pelo ID do usuário
    Optional<Bank> findByIdAndUserId(UUID id, UUID userId);
}