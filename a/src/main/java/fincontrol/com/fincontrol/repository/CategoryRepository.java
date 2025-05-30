package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByUserId(UUID userId);

    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    // Retorna o número de entidades deletadas, útil para verificar se a operação teve efeito
    long deleteByIdAndUserId(UUID id, UUID userId);
}