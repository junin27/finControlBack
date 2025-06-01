package fincontrol.com.fincontrol.model;

// IMPORTAÇÕES ADICIONADAS/REVISADAS
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
// import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Pode ser removido se @JsonIdentityInfo for usado consistentemente

import jakarta.persistence.*;
import lombok.Data; // Se @Data ainda for desejado, mantenha. Considere @Getter, @Setter, @ToString individualmente.
import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Se estiver usando @Data, certifique-se de que não está causando problemas com equals/hashCode em entidades JPA.
// Alternativamente, use @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @EqualsAndHashCode(of = "id"), @ToString(of = "id")
@Entity
@Table(name = "extra_incomes")
// ADICIONAR ESTA ANOTAÇÃO À CLASSE
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id" // "id" é o nome do campo de ID nesta entidade
)
public class ExtraIncome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate date;
}