package fincontrol.com.fincontrol.repository;

import fincontrol.com.fincontrol.model.Bill;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID>, JpaSpecificationExecutor<Bill> {

    Optional<Bill> findByIdAndUserId(UUID id, UUID userId);

    // Used by the service to get all bills for a user, then filters are applied via Specification
    // List<Bill> findAllByUserId(UUID userId); // This can be used if not using Specification for the base user filter

    // For the job to mark overdue bills
    List<Bill> findByStatusAndDueDateBefore(BillStatus status, LocalDate date);

    // For the automatic payment job
    List<Bill> findAllByAutoPayTrueAndStatusAndDueDateAndBankIsNotNull(
            BillStatus status,
            LocalDate dueDate
    );
}
