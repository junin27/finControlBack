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
import org.springframework.security.core.context.SecurityContextHolder; // Import mantido
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
    @Transactional // Mantido da feature branch
    public ExpenseDto create(ExpenseCreateDto dto, UUID userId) {
        // 1) Buscar categoria obrigatória
        Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId())); // Mensagem detalhada da feature branch

        // 2) Criar despesa e setar todos os campos obrigatórios
        Expense ex = new Expense();
        ex.setName(dto.getName());
        ex.setDescription(dto.getDescription());
        ex.setValue(dto.getValue());
        ex.setCategory(cat);

        // 3) Se vier bankId no DTO, buscar e setar
        if (dto.getBankId() != null) {
            Bank bank = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId())); // Mensagem detalhada da feature branch
            ex.setBank(bank);
        }

        // 4) Atribuir o User que está logado
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o ID: " + userId)); // Mensagem detalhada da feature branch
        ex.setUser(user);

        // 5) Salvar: aqui o JPA irá preencher createdAt e updatedAt
        Expense salvo = expenseRepo.save(ex);
        return toDto(salvo);
    }

    /** Lista todas as despesas do usuário logado */
    public List<ExpenseDto> listAll() { // Tipo de retorno já era ExpenseDto, o importante é a lógica interna
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Utilizando findAllByUserId do ExpenseRepository
        return expenseRepo.findAllByUserId(currentUser.getId()).stream()
                .map(this::toDto) // Correto, pois o stream é de Expense
                .collect(Collectors.toList());
    }

    /** Atualiza despesa existente */
    @Transactional
    public ExpenseDto update(UUID id, ExpenseUpdateDto dto) {
        // Verificação de permissão do usuário da feature branch
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada para o ID: " + id));

        if (!e.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta despesa não pertence ao usuário autenticado.");
        }

        if (dto.getName()        != null) e.setName(dto.getName());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getValue()       != null) e.setValue(dto.getValue());
        if (dto.getCategoryId()  != null) {
            Category cat = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId()));
            e.setCategory(cat);
        }
        if (dto.getBankId()      != null) {
            Bank b = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId()));
            e.setBank(b);
        } else {
            // Lógica opcional da feature branch para desvincular banco
            // e.setBank(null); // Descomente se quiser permitir desvincular o banco enviando bankId: null
        }

        return toDto(expenseRepo.save(e));
    }

    /** Deleta despesa */
    @Transactional // Mantido da feature branch
    public void delete(UUID id) {
        // Verificação de permissão do usuário da feature branch
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Expense expenseToDelete = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada para o ID: " + id));

        if (!expenseToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta despesa não pertence ao usuário autenticado.");
        }

        expenseRepo.deleteById(id);
    }

    private ExpenseDto toDto(Expense e) {
        UUID bankId = e.getBank()   != null ? e.getBank().getId()   : null;
        UUID categoryId = e.getCategory() != null ? e.getCategory().getId() : null;
        // Corrigido para pegar o 'name' da categoria, conforme a alteração recente na entidade Category
        String   categoryName = e.getCategory() != null ? e.getCategory().getName() : null;

        return new ExpenseDto(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getValue(),
                categoryId,
                categoryName, // Usando categoryName
                bankId,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}