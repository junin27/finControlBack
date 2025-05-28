package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Expense;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    // mais queries customizadas podem ser adicionadas aqui

    @Query("SELECT COALESCE(SUM(e.value),0) FROM Expense e WHERE e.bank.id = :bankId")
    BigDecimal sumExpenseByBank(@Param("bankId") UUID bankId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Expense e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

}
