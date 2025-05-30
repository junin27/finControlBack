package fincontrol.com.fincontrol.model;

import fincontrol.com.fincontrol.model.enums.PayableStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payable_installments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"payable_id", "installment_no"})
})
public class PayableInstallment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payable_id")
    private Payable payable;

    @Column(name = "installment_no", nullable = false)
    private int installmentNo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayableStatus status = PayableStatus.PENDING;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public PayableInstallment() {}

    // Getters e setters
}
