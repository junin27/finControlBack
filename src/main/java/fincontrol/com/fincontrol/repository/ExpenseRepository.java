package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // É uma boa prática adicionar @Repository

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // Importar Optional
import java.util.UUID;

@Repository // Adicionado @Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("SELECT COALESCE(SUM(e.value),0) FROM Expense e WHERE e.bank.id = :bankId")
    BigDecimal sumExpenseByBank(@Param("bankId") UUID bankId);

    @Modifying
    // @Transactional // Transacionalidade geralmente no Service
    @Query("DELETE FROM Expense e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

    List<Expense> findAllByUserId(UUID userId);

    // Método para buscar uma despesa específica por seu ID e pelo ID do usuário
    Optional<Expense> findByIdAndUserId(UUID id, UUID userId); // Este método é crucial

    boolean existsByCategoryId(UUID categoryId);

    @Modifying
    @Query("DELETE FROM Expense e WHERE e.user.id = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
}
