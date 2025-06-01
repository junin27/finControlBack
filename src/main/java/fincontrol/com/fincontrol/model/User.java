package fincontrol.com.fincontrol.model;

// IMPORTAÇÕES ADICIONADAS
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
// Adicione aqui as importações para as coleções se você as tiver (ex: List, Set)
// import java.util.List;
// import fincontrol.com.fincontrol.model.Bank;
// import fincontrol.com.fincontrol.model.ExtraIncome;
// import fincontrol.com.fincontrol.model.Expense;
// import fincontrol.com.fincontrol.model.Category;


@Getter
@Setter
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
// ADICIONAR ESTA ANOTAÇÃO À CLASSE
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id" // "id" é o nome do campo de ID nesta entidade
)
public class User {

    @Id
    @GeneratedValue // Se o seu UUID é gerado automaticamente, pode precisar de strategy = GenerationType.UUID
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    // Se você tiver relacionamentos bidirecionais mapeados aqui (ex: List<Bank>, List<Category>),
    // @JsonIdentityInfo ajudará a gerenciar a serialização deles também.
    // Exemplo:
    // @OneToMany(mappedBy = "user")
    // private List<Bank> banks;
    //
    // @OneToMany(mappedBy = "user") // Ou userId em Category, dependendo do mapeamento
    // private List<Category> categories;
    //
    // @OneToMany(mappedBy = "user")
    // private List<ExtraIncome> extraIncomes;
    //
    // @OneToMany(mappedBy = "user")
    // private List<Expense> expenses;


    public User() {}

    // Considere adicionar construtores, e se usar @Data do Lombok,
    // tenha cuidado com equals/hashCode em entidades com relacionamentos bidirecionais.
    // @Getter e @Setter individualmente são mais seguros.
}