package fincontrol.com.fincontrol.repository;


import fincontrol.com.fincontrol.model.Receivable;
import fincontrol.com.fincontrol.model.enums.ReceivableStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceivableRepository extends JpaRepository<Receivable, UUID>, JpaSpecificationExecutor<Receivable> {

    Optional<Receivable> findByIdAndUserId(UUID id, UUID userId);

    Page<Receivable> findAllByUserId(UUID userId, Pageable pageable);

    // For the job to mark overdue receivables
    List<Receivable> findAllByStatusAndDueDateBefore(ReceivableStatusEnum status, LocalDate date);

    // For the automatic bank receipt job
    List<Receivable> findAllByStatusAndAutomaticBankReceiptIsTrueAndDueDateIsLessThanEqual(
            ReceivableStatusEnum status,
            LocalDate dueDate
    );

    // Example for filtered listing
    @Query("SELECT r FROM Receivable r WHERE r.user.id = :userId " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:startDate IS NULL OR r.dueDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.dueDate <= :endDate)")
    Page<Receivable> findByUserAndFilters(
            @Param("userId") UUID userId,
            @Param("status") ReceivableStatusEnum status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}