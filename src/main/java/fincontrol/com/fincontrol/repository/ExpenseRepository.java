package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> { // Assumindo que o ID da Expense é UUID

    List<Expense> findAllByUserId(UUID userId);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e WHERE e.bank.id = :bankId AND e.user.id = :userId")
    BigDecimal sumExpenseByBankAndUser(@Param("bankId") UUID bankId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(e.value), 0) FROM Expense e WHERE e.bank.id = :bankId")
    BigDecimal sumExpenseByBank(@Param("bankId") UUID bankId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM Expense e WHERE e.bank.id = :bankId")
    void deleteByBankId(@Param("bankId") UUID bankId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int deleteAllByUserId(UUID userId);

    void deleteAllByUserAndBankId(User user, UUID bankId);
    boolean existsByCategoryIdAndUserId(UUID categoryId, UUID userId);



    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.bank WHERE e.user.id = :userId")
    List<Expense> findAllByUserIdWithBankDetails(@Param("userId") UUID userId);

    // ==>> ADICIONE ESTE MÉTODO ABAIXO <<==
    /**
     * Verifica se existem despesas associadas a um ID de categoria específico.
     * O nome do método "existsByCategory_Id" segue a convenção do Spring Data JPA
     * para verificar a existência baseada no campo 'id' da entidade 'category'
     * dentro da entidade Expense.
     *
     * @param categoryId O UUID da categoria a ser verificada.
     * @return true se existirem despesas para a categoria, false caso contrário.
     */
    boolean existsByCategory_Id(UUID categoryId);
    // Alternativamente, se o campo na entidade Expense for apenas 'category' e não 'category_id'
    // e 'category' tiver um campo 'id', o Spring Data JPA também entende:
    // boolean existsByCategoryId(UUID categoryId);
    // Teste "existsByCategory_Id" primeiro, pois é mais explícito sobre o campo aninhado.
    // Se sua entidade Expense tiver um campo direto chamado categoryId (como UUID), então
    // o nome do método "existsByCategoryId" já funcionaria diretamente.
    // Assumindo que Expense tem: private Category category; e Category tem private UUID id;
    boolean existsByCategoryId(UUID categoryId);

    /**
     * Soma do campo `amount` para todas as Expense com este bankId e pertencentes a este usuário.
     * Retorna 0 se não houver registros (COALESCE).
     */
    @Query("SELECT COALESCE(SUM(e.value), 0) " +
            "FROM Expense e " +
            "WHERE e.bank.id = :bankId " +
            "  AND e.user = :user")
    BigDecimal sumExpenseByBank(
            @Param("bankId") UUID bankId,
            @Param("user")   User user
    );
}