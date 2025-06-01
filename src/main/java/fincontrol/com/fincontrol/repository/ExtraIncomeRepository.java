package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExtraIncomeRepository extends JpaRepository<ExtraIncome, UUID> {

    // ---------------------------------------------------
    // Soma de amount por banco (via relacionamento) e usuário
    // ---------------------------------------------------
    @Query("SELECT COALESCE(SUM(e.amount), 0) " +
            "FROM ExtraIncome e " +
            "WHERE e.bank.id = :bankId " +
            "  AND e.user = :user")
    BigDecimal sumIncomeByBank(
            @Param("bankId") UUID bankId,
            @Param("user")   User user
    );

    // ---------------------------------------------------
    // Deleta todas as ExtraIncome de um dado usuário e dado bankId
    // ---------------------------------------------------
    void deleteAllByUserAndBankId(User user, UUID bankId);

    Optional<ExtraIncome> findByIdAndUserId(UUID id, UUID userId);

    // Se você também quiser manter o método que recebe diretamente o objeto User:

    /**
     * Soma do campo `amount` para todas as ExtraIncome com este bankId e pertencentes a este usuário.
     */



    // ---------------------------------------------------
    // Outros métodos existentes (findAllByUserAndBankId, etc.)
    // ---------------------------------------------------
    List<ExtraIncome> findAllByUser(User user);
    List<ExtraIncome> findAllByUserAndBankId(User user, UUID bankId);
    List<ExtraIncome> findAllByUserAndCategoryId(User user, UUID categoryId);
    Optional<ExtraIncome> findByIdAndUser(UUID id, User user);
    long countByUser(User user);
    void deleteAllByUser(User user);
    void deleteAllByUserAndCategoryId(User user, UUID categoryId);
}
