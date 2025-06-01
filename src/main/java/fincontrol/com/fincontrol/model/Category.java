package fincontrol.com.fincontrol.model;

// IMPORTAÇÕES ADICIONADAS/REVISADAS
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
// Adicione aqui as importações para as coleções se você as tiver (ex: List, Set)
// import java.util.List;
// import fincontrol.com.fincontrol.model.ExtraIncome;
// import fincontrol.com.fincontrol.model.Expense;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Mantida, pois é útil para proxies do Hibernate
@Entity
@Table(name = "categories")
@Data // Se estiver usando @Data, esteja ciente dos possíveis problemas com equals/hashCode em entidades.
@NoArgsConstructor
// ADICIONAR ESTA ANOTAÇÃO À CLASSE
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id" // "id" é o nome do campo de ID nesta entidade
)
public class Category {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", updatable = false, nullable = false) // Este é um ID, não a entidade User completa.
    // Se fosse @ManyToOne User user, @JsonIdentityInfo em User ajudaria.
    private UUID userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255, nullable = true)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Se você tivesse coleções aqui, como:
    // @OneToMany(mappedBy = "category")
    // private List<ExtraIncome> extraIncomes;
    //
    // @OneToMany(mappedBy = "category")
    // private List<Expense> expenses;
    // A anotação @JsonIdentityInfo na classe Category e nas classes ExtraIncome/Expense ajudaria.

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (!StringUtils.hasText(this.description)) {
            this.description = "Campo não Informado pelo Usuário";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}