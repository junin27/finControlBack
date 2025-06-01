package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExpenseCreateDto;
import fincontrol.com.fincontrol.dto.ExpenseMassUpdateDto;
import fincontrol.com.fincontrol.dto.ExpenseUpdateDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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

    private User findUserById(UUID userId) {
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
    public Expense create(ExpenseCreateDto dto, UUID userId) {
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
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Despesa '{}' (ID: {}) criada com valor {} para o usuário {}", savedExpense.getName(), savedExpense.getId(), savedExpense.getValue(), userId);
        return savedExpense;
    }

    @Transactional(readOnly = true)
    public List<Expense> listAllByAuthenticatedUser(UUID userId) {
        findUserById(userId);
        log.debug("Listando todas as despesas para o usuário {}", userId);
        return expenseRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Expense findByIdAndUserIdEnsureOwnership(UUID expenseId, UUID userId) {
        findUserById(userId);
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID " + expenseId + " não encontrada ou não pertence ao usuário."));
    }

    @Transactional
    public Expense update(UUID expenseId, ExpenseUpdateDto dto, UUID userId) {
        User user = findUserById(userId);
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa (ID: " + expenseId + ") não encontrada ou não pertence ao usuário para atualização."));

        BigDecimal oldValue = expense.getValue();
        Bank oldBank = expense.getBank();
        UUID oldBankId = (oldBank != null) ? oldBank.getId() : null;

        boolean needsSave = false;

        if (dto.getName() != null) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new InvalidOperationException("O nome da despesa, se fornecido para atualização, não pode ser vazio.");
            }
            if (!dto.getName().equals(expense.getName())) {
                expense.setName(dto.getName());
                needsSave = true;
            }
        }
        if (dto.getDescription() != null) {
            if (!dto.getDescription().equals(expense.getDescription())) {
                expense.setDescription(dto.getDescription());
                needsSave = true;
            }
        }
        if (dto.getValue() != null) {
            if (dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOperationException("O valor da despesa necessita ser maior que 0.");
            }
            if (expense.getValue().compareTo(dto.getValue()) != 0) {
                expense.setValue(dto.getValue());
                needsSave = true;
            }
        }
        if (dto.getExpenseDate() != null) {
            if (expense.getExpenseDate() == null || !dto.getExpenseDate().equals(expense.getExpenseDate())) {
                expense.setExpenseDate(dto.getExpenseDate());
                needsSave = true;
            }
        }
        if (dto.getCategoryId() != null) {
            if (expense.getCategory() == null || !dto.getCategoryId().equals(expense.getCategory().getId())) {
                Category category = findCategoryByIdAndUser(dto.getCategoryId(), userId);
                expense.setCategory(category);
                needsSave = true;
            }
        }

        Bank newBank = null;
        if (dto.getBankId() != null) {
            newBank = findBankByIdAndUser(dto.getBankId(), userId);
        }

        boolean bankChanged = (oldBankId == null && dto.getBankId() != null) ||
                (oldBankId != null && dto.getBankId() == null) ||
                (oldBankId != null && dto.getBankId() != null && !oldBankId.equals(dto.getBankId()));

        if (bankChanged) {
            needsSave = true;
        }
        expense.setBank(newBank);

        if (needsSave) {
            Expense updatedExpense = expenseRepository.save(expense);
            log.info("Despesa '{}' (ID: {}) atualizada para o usuário {}", updatedExpense.getName(), expenseId, userId);
            return updatedExpense;
        }
        return expense;
    }

    @Transactional
    public void delete(UUID expenseId, UUID userId) {
        findUserById(userId);
        Expense expenseToDelete = expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa (ID: " + expenseId + ") não encontrada ou não pertence ao usuário para exclusão."));

        // Removida alteração automática do saldo do banco

        expenseRepository.delete(expenseToDelete);
        log.info("Despesa (ID: {}) deletada para o usuário {}", expenseId, userId);
    }

    @Transactional
    public List<Expense> massUpdateUserExpenses(ExpenseMassUpdateDto dto, UUID userId) {
        findUserById(userId);
        List<Expense> userExpenses = expenseRepository.findAllByUserId(userId);

        if (userExpenses.isEmpty()) {
            log.info("Nenhuma despesa encontrada para atualização em massa para o usuário {}", userId);
            return new ArrayList<>();
        }

        boolean descriptionProvided = dto.getDescription() != null;
        boolean expenseDateProvided = dto.getExpenseDate() != null;

        if (!descriptionProvided && !expenseDateProvided) {
            log.info("Nenhum campo fornecido para atualização em massa de despesas para o usuário {}", userId);
            return userExpenses;
        }

        List<Expense> modifiedExpenses = new ArrayList<>();
        for (Expense expense : userExpenses) {
            boolean modifiedThisExpense = false;
            if (descriptionProvided && (expense.getDescription() == null || !dto.getDescription().equals(expense.getDescription()))) {
                expense.setDescription(dto.getDescription());
                modifiedThisExpense = true;
            }
            if (expenseDateProvided && (expense.getExpenseDate() == null || !dto.getExpenseDate().equals(expense.getExpenseDate()))) {
                expense.setExpenseDate(dto.getExpenseDate());
                modifiedThisExpense = true;
            }
            if (modifiedThisExpense) {
                modifiedExpenses.add(expense);
            }
        }

        if (!modifiedExpenses.isEmpty()) {
            log.info("Atualizando {} despesas em massa para o usuário {}", modifiedExpenses.size(), userId);
            return expenseRepository.saveAll(modifiedExpenses);
        }
        return userExpenses;
    }

    @Transactional
    public int deleteAllUserExpenses(UUID userId) {
        findUserById(userId);

        int deletedCount = expenseRepository.deleteAllByUserId(userId);
        log.info("{} despesas do usuário {} foram deletadas.", deletedCount, userId);
        return deletedCount;
    }
}
