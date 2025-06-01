package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.ExtraIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; // Importe Optional
import java.util.UUID;

public interface ExtraIncomeRepository extends JpaRepository<ExtraIncome, Long> {
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExtraIncome e WHERE e.bank.id = :bankId")
    BigDecimal sumIncomeByBank(@Param("bankId") UUID bankId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExtraIncome e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

    List<ExtraIncome> findByBankId(UUID bankId);
    List<ExtraIncome> findByUserId(UUID userId);

    @Query("SELECT ei FROM ExtraIncome ei LEFT JOIN FETCH ei.bank WHERE ei.user.id = :userId")
    List<ExtraIncome> findByUserIdWithBank(@Param("userId") UUID userId);

    @Query("SELECT ei FROM ExtraIncome ei LEFT JOIN FETCH ei.bank WHERE ei.bank.id = :bankId AND ei.user.id = :userId")
    List<ExtraIncome> findByBankIdAndUserIdWithBank(@Param("bankId") UUID bankId, @Param("userId") UUID userId);

    // Adicione este método:
    // Ele busca uma ExtraIncome pelo seu ID e pelo ID do usuário associado.
    // O Spring Data JPA irá gerar a query automaticamente com base no nome do método.
    Optional<ExtraIncome> findByIdAndUserId(Long id, UUID userId);
}