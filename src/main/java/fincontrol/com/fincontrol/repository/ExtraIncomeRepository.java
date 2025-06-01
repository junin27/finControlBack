package fincontrol.com.fincontrol.repository; // Certifique-se que este é o pacote correto

import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtraIncomeRepository extends JpaRepository<ExtraIncome, UUID> {

    // Métodos que seu ExtraIncomeService já parece usar ou pode precisar:
    List<ExtraIncome> findAllByUser(User user);

    Optional<ExtraIncome> findByIdAndUser(UUID id, User user);

    List<ExtraIncome> findAllByUserAndBankId(User user, UUID bankId);

    List<ExtraIncome> findAllByUserAndCategoryId(User user, UUID categoryId); // Supondo que categoryId seja um campo em ExtraIncome

    long countByUser(User user);

    @Modifying
    @Transactional
    void deleteAllByUser(User user);

    @Modifying
    @Transactional
    void deleteAllByUserAndBankId(User user, UUID bankId);

    @Modifying
    @Transactional
    void deleteAllByUserAndCategoryId(User user, UUID categoryId); // Supondo que categoryId seja um campo

    // Método que estava causando o erro "cannot find symbol"
    // Esta é a definição correta com a anotação @Query
    @Query("SELECT ei FROM ExtraIncome ei WHERE ei.id IN :ids AND ei.user = :user")
    List<ExtraIncome> findAllByIdInAndUser(@Param("ids") List<UUID> ids, @Param("user") User user);

    // Métodos de soma que você já tinha, mantidos aqui:
    @Query("SELECT COALESCE(SUM(ei.amount), 0) FROM ExtraIncome ei WHERE ei.bank.id = :bankId AND ei.user = :user")
    BigDecimal sumIncomeByBankAndUser(@Param("bankId") UUID bankId, @Param("user") User user);

    @Query("SELECT COALESCE(SUM(ei.amount), 0) FROM ExtraIncome ei WHERE ei.bank.id = :bankId AND ei.user.id = :userId")
    BigDecimal sumIncomeByBankIdAndUserId(@Param("bankId") UUID bankId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(ei.amount), 0) FROM ExtraIncome ei WHERE ei.bank.id = :bankId AND ei.user = :user")
    BigDecimal sumIncomeByBank(@Param("bankId") UUID bankId, @Param("user") User user);

}