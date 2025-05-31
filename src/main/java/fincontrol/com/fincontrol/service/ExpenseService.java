package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExpenseCreateDto;
import fincontrol.com.fincontrol.dto.ExpenseMassUpdateDto;
import fincontrol.com.fincontrol.dto.ExpenseUpdateDto;
// Não precisa mais retornar ExpenseDto daqui, o controller vai montar o ExpenseDetailResponseDto
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
// import org.springframework.security.core.context.SecurityContextHolder; // Controller vai pegar o User
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
// import java.time.LocalDate; // Não é mais necessário importar se não usado diretamente nos parâmetros aqui
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// import java.util.stream.Collectors; // Não é mais necessário aqui

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BankRepository bankRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          BankRepository bankRepository,
                          UserRepository userRepository) {
        this.expenseRepository  = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.bankRepository     = bankRepository;
        this.userRepository     = userRepository;
    }

    private User findUserById(UUID userId) { // Mantido para validação interna
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
    }

    private Category findCategoryByIdAndUser(UUID categoryId, UUID userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria com ID " + categoryId + " não encontrada ou não pertence ao usuário."));
    }

    private Bank findBankByIdAndUser(UUID bankId, UUID userId) {
        return bankRepository.findByIdAndUserId(bankId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Banco com ID " + bankId + " não encontrado ou não pertence ao usuário."));
    }

    @Transactional
    public Expense create(ExpenseCreateDto dto, UUID userId) { // Retorna a Entidade
        User user = findUserById(userId);
        Category category = findCategoryByIdAndUser(dto.getCategoryId(), userId);

        Expense expense = new Expense();
        expense.setName(dto.getName());
        expense.setDescription(dto.getDescription());
        expense.setValue(dto.getValue());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setCategory(category);
        expense.setUser(user);

        if (dto.getBankId() != null) {
            Bank bank = findBankByIdAndUser(dto.getBankId(), userId);
            expense.setBank(bank);
        }
        return expenseRepository.save(expense); // Retorna a entidade salva
    }

    public List<Expense> listAllByAuthenticatedUser(UUID userId) { // Recebe userId, retorna Lista de Entidades
        findUserById(userId); // Valida usuário
        return expenseRepository.findAllByUserId(userId);
    }

    public Expense findByIdAndUserIdEnsureOwnership(UUID expenseId, UUID userId) { // Retorna a Entidade
        findUserById(userId); // Valida usuário
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID " + expenseId + " não encontrada ou não pertence ao usuário."));
    }

    @Transactional
    public Expense update(UUID expenseId, ExpenseUpdateDto dto, UUID userId) { // Retorna a Entidade
        findUserById(userId);
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Não é possível editar a despesa (ID: " + expenseId + "), porque você não possui ela cadastrada."));

        boolean needsUpdate = false;
        // ... (lógica de atualização idêntica à anterior, modificando a entidade 'expense') ...
        if (dto.getName() != null) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new InvalidOperationException("O nome da despesa, se fornecido para atualização, não pode ser vazio.");
            }
            if(!dto.getName().equals(expense.getName())){
                expense.setName(dto.getName());
                needsUpdate = true;
            }
        }
        if (dto.getDescription() != null) {
            if(!dto.getDescription().equals(expense.getDescription())) {
                expense.setDescription(dto.getDescription());
                needsUpdate = true;
            }
        }
        if (dto.getValue() != null) {
            if (dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("O campo value necessita ser maior que 0.");
            }
            if(expense.getValue().compareTo(dto.getValue()) != 0) {
                expense.setValue(dto.getValue());
                needsUpdate = true;
            }
        }
        if (dto.getExpenseDate() != null || expense.getExpenseDate() != null) { // Permite setar para null para limpar
            if (expense.getExpenseDate() == null || dto.getExpenseDate() == null || !dto.getExpenseDate().equals(expense.getExpenseDate())) {
                expense.setExpenseDate(dto.getExpenseDate());
                needsUpdate = true;
            }
        }
        if (dto.getCategoryId()  != null) {
            if (expense.getCategory() == null || !dto.getCategoryId().equals(expense.getCategory().getId())) {
                Category category = findCategoryByIdAndUser(dto.getCategoryId(), userId);
                expense.setCategory(category);
                needsUpdate = true;
            }
        }
        if (dto.getBankId() != null) {
            if (expense.getBank() == null || !dto.getBankId().equals(expense.getBank().getId())) {
                Bank bank = findBankByIdAndUser(dto.getBankId(), userId);
                expense.setBank(bank);
                needsUpdate = true;
            }
        } else {
            if (expense.getBank() != null) {
                expense.setBank(null);
                needsUpdate = true;
            }
        }

        if(needsUpdate) {
            return expenseRepository.save(expense);
        }
        return expense;
    }

    @Transactional
    public void delete(UUID expenseId, UUID userId) {
        findUserById(userId);
        Expense expenseToDelete = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Não é possível deletar a despesa (ID: " + expenseId + "), porque você não possui ela cadastrada."));

        expenseRepository.delete(expenseToDelete);
    }

    @Transactional
    public List<Expense> massUpdateUserExpenses(ExpenseMassUpdateDto dto, UUID userId) { // Retorna Lista de Entidades
        findUserById(userId);
        List<Expense> userExpenses = expenseRepository.findAllByUserId(userId);

        if (userExpenses.isEmpty()) {
            return new ArrayList<>();
        }

        boolean descriptionProvided = dto.getDescription() != null;
        boolean expenseDateProvided = dto.getExpenseDate() != null;

        if (!descriptionProvided && !expenseDateProvided) {
            return userExpenses;
        }

        for (Expense expense : userExpenses) {
            boolean modified = false;
            if (descriptionProvided && (expense.getDescription() == null || !dto.getDescription().equals(expense.getDescription()))) {
                expense.setDescription(dto.getDescription());
                modified = true;
            }
            if (expenseDateProvided && (expense.getExpenseDate() == null || !dto.getExpenseDate().equals(expense.getExpenseDate()))) {
                expense.setExpenseDate(dto.getExpenseDate());
                modified = true;
            }
            // A entidade @PreUpdate cuidará de updatedAt se modified for true e save for chamado.
            // Mas saveAll fará isso de qualquer forma se a entidade for considerada suja pelo Hibernate.
        }

        return expenseRepository.saveAll(userExpenses); // Salva todas, mesmo que algumas não tenham mudado, para simplicidade.
        // O Hibernate otimiza se não houver mudanças reais.
    }

    @Transactional
    public int deleteAllUserExpenses(UUID userId) {
        findUserById(userId);
        return expenseRepository.deleteAllByUserId(userId);
    }


}
