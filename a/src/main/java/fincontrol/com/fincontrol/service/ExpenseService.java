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
import org.springframework.util.StringUtils; // Import para StringUtils

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

    @Transactional
    public ExpenseDto create(ExpenseCreateDto dto, UUID userId) {
        Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId()));

        Expense ex = new Expense();
        ex.setName(dto.getName());
        ex.setDescription(dto.getDescription()); // Será tratado pelo @PrePersist se for null/vazio
        ex.setValue(dto.getValue());
        ex.setCategory(cat);
        ex.setExpenseDate(dto.getExpenseDate()); // Define a data da despesa

        if (dto.getBankId() != null) {
            Bank bank = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId()));
            ex.setBank(bank);
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o ID: " + userId));
        ex.setUser(user);

        Expense salvo = expenseRepo.save(ex);
        return toDto(salvo);
    }

    public List<ExpenseDto> listAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return expenseRepo.findAllByUserId(currentUser.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseDto update(UUID id, ExpenseUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada para o ID: " + id));

        if (!e.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta despesa não pertence ao usuário autenticado.");
        }

        boolean needsUpdate = false;

        if (dto.getName() != null && StringUtils.hasText(dto.getName())) {
            if(!dto.getName().equals(e.getName())){
                e.setName(dto.getName());
                needsUpdate = true;
            }
        }
        if (dto.getDescription() != null) { // Permite limpar description se "" for enviado
            if(!dto.getDescription().equals(e.getDescription())) {
                e.setDescription(dto.getDescription());
                needsUpdate = true;
            }
        }
        if (dto.getValue() != null) {
            if(e.getValue().compareTo(dto.getValue()) != 0) {
                e.setValue(dto.getValue());
                needsUpdate = true;
            }
        }
        if (dto.getExpenseDate() != e.getExpenseDate() &&
           (dto.getExpenseDate() != null || e.getExpenseDate() != null)) {
            e.setExpenseDate(dto.getExpenseDate()); // Permite setar para null para limpar a data
            needsUpdate = true;
        }

        if (dto.getCategoryId()  != null) {
            if (e.getCategory() == null || !dto.getCategoryId().equals(e.getCategory().getId())) {
                Category cat = categoryRepo.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId()));
                e.setCategory(cat);
                needsUpdate = true;
            }
        }

        // Lógica para atualizar ou desassociar o banco
        if (dto.getBankId() != null) { // Se um bankId foi fornecido no DTO para associar/mudar
            if (e.getBank() == null || !dto.getBankId().equals(e.getBank().getId())) {
                Bank b = bankRepo.findById(dto.getBankId())
                        .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId()));
                e.setBank(b);
                needsUpdate = true;
            }
        } else {
            // Se o bankId NÃO está no DTO (ou seja, dto.getBankId() é null),
            // mas a despesa *estava* associada a um banco, isso significa que queremos desassociar.
            if (e.getBank() != null) { // Apenas desassocie se havia um banco antes
                e.setBank(null);
                needsUpdate = true;
            }
        }

        if(needsUpdate) {
            return toDto(expenseRepo.save(e));
        }
        return toDto(e);
    }

    @Transactional
    public void delete(UUID id) {
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
        UUID bankId = null;
        String bankDisplayName;

        if (e.getBank() != null) {
            bankId = e.getBank().getId();
            if (StringUtils.hasText(e.getBank().getName())) {
                bankDisplayName = e.getBank().getName();
            } else {
                bankDisplayName = "Banco sem Nome Definido";
            }
        } else {
            bankDisplayName = "Banco não Informado pelo Usuário";
        }

        UUID categoryId = e.getCategory() != null ? e.getCategory().getId() : null;
        String categoryName = e.getCategory() != null ? e.getCategory().getName() : "Categoria não Informada";


        return new ExpenseDto(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getValue(),
                e.getExpenseDate(), // Adicionado expenseDate
                categoryId,
                categoryName,
                bankId,
                bankDisplayName,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}