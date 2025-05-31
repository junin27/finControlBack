package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByUserId(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    long deleteByIdAndUserId(UUID id, UUID userId);

    // Novo método para deletar todas as categorias de um usuário
    // Retorna o número de categorias deletadas
    @Modifying
    @Query("DELETE FROM Category c WHERE c.userId = :userId")
    int deleteAllByUserId(@Param("userId") UUID userId);
}