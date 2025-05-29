package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaultRepository extends JpaRepository<Vault, UUID> {

    List<Vault> findAllByUserId(UUID userId);

    Optional<Vault> findByIdAndUserId(UUID id, UUID userId);

    List<Vault> findAllByBankIdAndUserId(UUID bankId, UUID userId);
}