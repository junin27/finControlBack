package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.ExtraIncome;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExtraIncomeRepository extends JpaRepository<ExtraIncome, Long> {

    List<ExtraIncome> findByUserId(java.util.UUID userId);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM ExtraIncome e WHERE e.bank.id = :bankId")
    BigDecimal sumIncomeByBank(@Param("bankId") UUID bankId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExtraIncome e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

    Optional<ExtraIncome> findByIdAndUserId(Long id, UUID userId);

}
