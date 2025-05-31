package fincontrol.com.fincontrol.model;

import fincontrol.com.fincontrol.model.enums.ReceiptMethodEnum;
import fincontrol.com.fincontrol.model.enums.ReceivableStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "receivables")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_income_id", nullable = false)
    private ExtraIncome extraIncome; // Renda Extra Associada

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReceiptMethodEnum receiptMethod; // Meio de Recebimento

    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate; // Data de Recebimento (Prevista)

    @NotNull
    @Column(nullable = false)
    private Boolean automaticBankReceipt; // Recebimento Automático com Banco

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReceivableStatusEnum status; // Status (Gerenciado pelo Sistema)

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}