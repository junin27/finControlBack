package fincontrol.com.fincontrol.model;

import fincontrol.com.fincontrol.model.enums.PayableStatus;
import fincontrol.com.fincontrol.model.enums.PaymentMethodType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payables")
public class Payable {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // opcional

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "amount_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTotal;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayableStatus status = PayableStatus.PENDING;

    @Column(name = "total_installments", nullable = false)
    private int totalInstallments = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodType paymentMethod; // opcional

    @Column(name = "paid_date")
    private LocalDate paidDate; // opcional

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public Payable() {}

    // Getters e setters
}
