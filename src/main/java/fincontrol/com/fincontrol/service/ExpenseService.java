package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExpenseCreateDto;
import fincontrol.com.fincontrol.dto.ExpenseDto;
import fincontrol.com.fincontrol.dto.ExpenseUpdateDto;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final CategoryRepository categoryRepo;
    private final BankRepository bankRepo;
    private final UserRepository userRepo;

    public ExpenseService(ExpenseRepository e,
                          CategoryRepository c,
                          BankRepository b,
                          UserRepository u) {
        this.expenseRepo  = e;
        this.categoryRepo = c;
        this.bankRepo     = b;
        this.userRepo     = u;
    }

    /** Cria nova despesa */
    public ExpenseDto create(ExpenseCreateDto dto, UUID userId) {
        // 1) Buscar categoria obrigatória
        Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        // 2) Criar despesa e setar todos os campos obrigatórios
        Expense ex = new Expense();
        ex.setName(dto.getName());
        ex.setDescription(dto.getDescription());
        ex.setValue(dto.getValue());
        ex.setCategory(cat);

        // 3) Se vier bankId no DTO, buscar e setar
        if (dto.getBankId() != null) {
            Bank bank = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado"));
            ex.setBank(bank);
        }

        // 4) Atribuir o User que está logado (pegue o userId do token/jwt)
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        ex.setUser(user);

        // 5) Salvar: aqui o JPA irá preencher createdAt e updatedAt
        Expense salvo = expenseRepo.save(ex);
        return toDto(salvo);
    }

    /** Lista todas as despesas */
    public List<ExpenseDto> listAll() {
        return expenseRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** Atualiza despesa existente */
    @Transactional
    public ExpenseDto update(UUID id, ExpenseUpdateDto dto) {
        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        if (dto.getName()        != null) e.setName(dto.getName());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getValue()       != null) e.setValue(dto.getValue());
        if (dto.getCategoryId()  != null) {
            Category cat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            e.setCategory(cat);
        }
        if (dto.getBankId()      != null) {
            Bank b = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado"));
            e.setBank(b);
        }

        // ao salvar, o JPA irá atualizar o campo updatedAt automaticamente
        return toDto(expenseRepo.save(e));
    }

    /** Deleta despesa */
    public void delete(UUID id) {
        expenseRepo.deleteById(id);
    }

    private ExpenseDto toDto(Expense e) {
        UUID bankId = e.getBank()   != null ? e.getBank().getId()   : null;
        UUID categoryId = e.getCategory() != null ? e.getCategory().getId() : null;
        String   categoryDesc = e.getCategory() != null ? e.getCategory().getDescription() : null;

        return new ExpenseDto(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getValue(),
                categoryId,
                categoryDesc,
                bankId,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }


}
