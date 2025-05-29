package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.BankCreateDto;
import fincontrol.com.fincontrol.dto.BankDto;
import fincontrol.com.fincontrol.dto.BankUpdateDto;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankService {
    private final BankRepository bankRepo;
    private final ExtraIncomeRepository incomeRepo;
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public BankService(BankRepository b,
                       ExtraIncomeRepository i,
                       ExpenseRepository e,
                       UserRepository u) {
        this.bankRepo   = b;
        this.incomeRepo = i;
        this.expenseRepo= e;
        this.userRepo   = u;
    }

    @Transactional
    public BankDto create(BankCreateDto dto) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bank b = new Bank();
        b.setName(dto.getName());
        b.setDescription(dto.getDescription());
        b.setUser(u);

        if (dto.getInitialBalance() != null) {
            b.setBalance(dto.getInitialBalance());
        }
        // createdAt e updatedAt serão preenchidos pelo @EntityListeners(AuditingEntityListener.class) na entidade Bank

        bankRepo.save(b);

        return toDto(b);
    }

    public List<BankDto> listAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return bankRepo.findAllByUserId(currentUser.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BankDto update(UUID id, BankUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));

        if (!b.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Este banco não pertence ao usuário autenticado.");
        }

        boolean needsUpdate = false;

        if (dto.getName() != null) {
            if (!dto.getName().equals(b.getName())) {
                b.setName(dto.getName());
                needsUpdate = true;
            }
        }
        if (dto.getDescription() != null) {
            if (!dto.getDescription().equals(b.getDescription())) {
                b.setDescription(dto.getDescription());
                needsUpdate = true;
            }
        }
        // Adiciona a lógica para atualizar o saldo se fornecido
        if (dto.getBalance() != null) {
            if (b.getBalance().compareTo(dto.getBalance()) != 0) { // Compara BigDecimals corretamente
                b.setBalance(dto.getBalance());
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            bankRepo.save(b);
        }
        return toDto(b);
    }

    @Transactional
    public void deleteAll(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado para deletar"));

        if (!b.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Este banco não pertence ao usuário autenticado.");
        }
        bankRepo.deleteById(id);
    }

    @Transactional
    public void clearIncomes(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));

        if (!b.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Este banco não pertence ao usuário autenticado.");
        }

        BigDecimal totalIncomesCleared = incomeRepo.sumIncomeByBank(id);
        if (totalIncomesCleared == null) totalIncomesCleared = BigDecimal.ZERO;

        incomeRepo.deleteByBankId(id);

        b.setBalance(b.getBalance().subtract(totalIncomesCleared));
        bankRepo.save(b); // Salvar o banco para atualizar o saldo e o updatedAt
    }

    @Transactional
    public void clearExpenses(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));

        if (!b.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Este banco não pertence ao usuário autenticado.");
        }

        BigDecimal totalExpensesCleared = expenseRepo.sumExpenseByBank(id);
        if (totalExpensesCleared == null) totalExpensesCleared = BigDecimal.ZERO;

        expenseRepo.deleteByBankId(id);

        b.setBalance(b.getBalance().add(totalExpensesCleared));
        bankRepo.save(b); // Salvar o banco para atualizar o saldo e o updatedAt
    }

    private BankDto toDto(Bank b) {
        BigDecimal totalIn = incomeRepo.sumIncomeByBank(b.getId());
        if (totalIn == null) totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = expenseRepo.sumExpenseByBank(b.getId());
        if (totalOut == null) totalOut = BigDecimal.ZERO;

        return new BankDto(
                b.getId(),
                b.getName(),
                b.getDescription(),
                totalIn,
                totalOut,
                b.getBalance(),
                b.getCreatedAt(),   // Adicionado createdAt
                b.getUpdatedAt()    // Adicionado updatedAt
        );
    }
}