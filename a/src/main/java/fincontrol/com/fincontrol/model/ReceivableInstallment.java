package fincontrol.com.fincontrol.model;

import fincontrol.com.fincontrol.model.enums.ReceivableStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "receivable_installments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"receivable_id", "installment_no"})
})
public class ReceivableInstallment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receivable_id")
    private Receivable receivable;

    @Column(name = "installment_no", nullable = false)
    private int installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "received_amount", precision = 12, scale = 2)
    private BigDecimal receivedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivableStatus status = ReceivableStatus.PENDING;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public ReceivableInstallment() {}

    // Getters e setters
}
