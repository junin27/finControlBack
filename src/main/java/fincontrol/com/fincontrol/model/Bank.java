package fincontrol.com.fincontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils; // Import da sua branch 'feature'

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@Entity
@EntityListeners(AuditingEntityListener.class) // Mantendo a anotação limpa
@Table(name = "banks")
public class Bank {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255) // Mantendo nullable = true por padrão, o @PrePersist lida com o default
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false) // Mantendo nullable = false da sua branch 'feature'
    private LocalDateTime updatedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExtraIncome> incomes;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses;

    @PrePersist
    protected void onPrePersist() {
        // Define o valor padrão para 'description' se não for informado ou for vazio/só espaços
        if (!StringUtils.hasText(this.description)) {
            this.description = "Campo não Informado pelo Usuário";
        }
    }
}