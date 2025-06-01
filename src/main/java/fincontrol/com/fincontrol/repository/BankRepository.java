package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Bank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import Modifying
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    // Method to find all banks by User ID
    List<Bank> findAllByUserId(UUID userId);



    // Method to delete all banks by User ID
    // This is more efficient than fetching all entities and then deleting them one by one or in a list.
    // Ensure cascading deletes are configured correctly on the Bank entity for related incomes/expenses.
    @Modifying // Indicates that this query will change data
    @Query("DELETE FROM Bank b WHERE b.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Bank> findById(UUID id);

    @Query("SELECT b FROM Bank b JOIN FETCH b.user WHERE b.id = :id AND b.user.id = :userId")
    Optional<Bank> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}