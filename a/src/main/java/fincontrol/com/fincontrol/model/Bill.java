package fincontrol.com.fincontrol.model;

import fincontrol.com.fincontrol.model.enums.PaymentMethod;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bills") // Renamed table to "bills"
public class Bill { // Renamed class to "Bill"

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false) // Foreign key to Expense
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY) // Optional
    @JoinColumn(name = "bank_id", nullable = true)
    private Bank bank;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "auto_pay", nullable = false)
    private boolean autoPay = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BillStatus status = BillStatus.PENDING;

    @Column(name = "payment_date", nullable = true)
    private LocalDate paymentDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
