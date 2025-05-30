package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Expense; // Garanta que este import está correto
import org.springframework.data.jpa.repository.JpaRepository; // Import principal
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;    // Import que adicionamos
import java.util.Optional; // << ADICIONADO: Import para Optional
import java.util.UUID;    // Import para UUID

public interface ExpenseRepository extends JpaRepository<Expense, UUID> { // Garanta que Expense e UUID estão corretos aqui

    @Query("SELECT COALESCE(SUM(e.value),0) FROM Expense e WHERE e.bank.id = :bankId")
    BigDecimal sumExpenseByBank(@Param("bankId") UUID bankId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Expense e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

    List<Expense> findAllByUserId(UUID userId); // Método que adicionamos

    // MÉTODO QUE FALTAVA:
    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);
}